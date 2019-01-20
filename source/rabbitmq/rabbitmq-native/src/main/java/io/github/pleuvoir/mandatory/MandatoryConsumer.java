package io.github.pleuvoir.mandatory;

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

public class MandatoryConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.MANDATORY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		String queueName = channel.queueDeclare().getQueue();

		// 只关注error级别的日志
		String severity = "error";
		channel.queueBind(queueName, Const.MANDATORY_EXCHANGE_NAME, severity);

		System.out.println(" [*] Waiting for messages......");

		// 创建队列消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				System.out.println("Received [" + envelope.getRoutingKey() + "] " + new String(body, "UTF-8"));
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}

}
