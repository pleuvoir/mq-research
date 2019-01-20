package io.github.pleuvoir.exchange.topic;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * B 机器微信错误消息
 * @author pleuvoir
 *
 */
public class BWechatErrorConsumer {

	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(Const.TOPIC_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
		String queueName = channel.queueDeclare().getQueue();

		channel.queueBind(queueName, Const.TOPIC_EXCHANGE_NAME, "B.WECHAT.ERROR");

		System.out.println("BWechatErrorConsumer 等待接收消息 ..");

		DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("接受到路由键[" + envelope.getRoutingKey() + "]的消息，" + new String(body, "UTF-8"));
			}
		};

		channel.basicConsume(queueName, true, defaultConsumer);
	}
	
}
