package io.github.pleuvoir.deadletter;

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
 * 死信消息消费者  和普通的消费者无异
 * @author pleuvoir
 *
 */
public class DLXConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();

		Channel channel = connection.createChannel();

		channel.exchangeDeclare(Const.DLX.BEGIN_ARRIAL_EXCHANGE, BuiltinExchangeType.DIRECT);
		
		channel.queueDeclare(Const.DLX.BEGIN_ARRIAL_EXCHANGE, false, false, false, null);
		
		channel.queueBind(Const.DLX.BEGIN_ARRIAL_QUEUE, Const.DLX.BEGIN_ARRIAL_EXCHANGE, Const.DLX.BEGIN_ARRIAL_ROUTEKEY);

		System.out.println("DLXConsumer 等待接收消息 ........");

		// 声明了一个消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("死信消息消费者：接受到路由键[" + envelope.getRoutingKey() + "]" + message);
			}
		};
		channel.basicConsume(Const.DLX.BEGIN_ARRIAL_QUEUE, true, consumer);
	}
	
}

