package io.github.pleuvoir.producerconfirm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.github.pleuvoir.kit.Const;

/**
 * 异步确认消费者
 * @author pleuvoir
 *
 */
public class AsyncConfirmConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		// 创建连接，该地址为阿里云服务器地址 已开放 guest 远程访问权限
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("39.105.110.40");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		Connection connection = connectionFactory.newConnection();

		// 打开连接和创建频道，与发送端一样
		Channel channel = connection.createChannel();
		// 创建持久化交换器，当生产者先发送消息 消费者再上线 则可以接收到 以前的消息
		channel.exchangeDeclare(Const.PRODUCER_ASYNC_CONFIRM_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

		// 声明一个队列，注意队列是在消费者端声明的
		String queueName = "async_confirm_mode_test_queue";
		channel.queueDeclare(queueName, false, false, false, null);

		// 绑定，将队列和交换器通过路由键进行绑定 表示只关注 error 级别的日志消息
		String routekey = "error";
		channel.queueBind(queueName, Const.PRODUCER_ASYNC_CONFIRM_EXCHANGE_NAME, routekey);

		System.out.println("异步确认消费者等待接收消息 ........");

		// 声明了一个消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("接受到路由键[" + envelope.getRoutingKey() + "]" + message);
			}
		};
		// 消费者正式开始在指定队列上消费消息 ， true 代表自动 ack
		channel.basicConsume(queueName, true, consumer);
	}

}
