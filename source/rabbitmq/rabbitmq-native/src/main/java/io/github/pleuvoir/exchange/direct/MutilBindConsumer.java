package io.github.pleuvoir.exchange.direct;

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
 * 队列和交换器的多重绑定，使用 direct 类型交换器 <b/>
 * RabbitMQ 支持一个队列绑定多个路由键
 * @author pleuvoir
 *
 */
public class MutilBindConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		// 创建连接，该地址为阿里云服务器地址 已开放 guest 远程访问权限
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("39.105.110.40");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		Connection connection = connectionFactory.newConnection();

		// 打开连接和创建信道，与发送端一样
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.DIRECT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// #########
		//  上面的部分和生产者完全一样
		// ########
		
		// 随机生成一个队列，如果在声明时未起名则会生成一个 amq.gen- 开头的队列
		String queueName = channel.queueDeclare().getQueue();
		//				   channel.queueDeclare(queueName, false, false, false, null);

		System.out.println("随机队列，" + queueName);
		// 绑定，将队列和交换器通过路由键进行绑定   关注 error warning 级别的日志消息
		channel.queueBind(queueName, Const.DIRECT_EXCHANGE_NAME, "error");
		channel.queueBind(queueName, Const.DIRECT_EXCHANGE_NAME, "warning");

		System.out.println("等待接收消息 ........");

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
