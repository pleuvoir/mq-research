package io.github.pleuvoir.transaction;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 事务消费者
 * @author pleuvoir
 *
 */
public class TransactionConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		// 创建连接，该地址为阿里云服务器地址 已开放 guest 远程访问权限
		Connection connection = RabbitMQKit.createConnection();

		// 打开连接和创建频道，与发送端一样
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.TRANSACTION_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		String queueName = "producer_confirm";
		channel.queueDeclare(queueName, false, false, false, null);

		// 绑定，将队列和交换器通过路由键进行绑定 表示只关注 error 级别的日志消息
		String routekey = "error";
		channel.queueBind(queueName, Const.TRANSACTION_EXCHANGE_NAME, routekey);

		System.out.println("事务消费者等待接收消息 ........");

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
