package io.github.pleuvoir.requestresponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

import io.github.pleuvoir.kit.RabbitMQKit;

public class Consumer {

	static String QUEUE = "replyto";
	
	public static void main(String[] args) throws IOException, TimeoutException {
		Channel channel = RabbitMQKit.createConnection().createChannel();

		channel.exchangeDeclare(Producer.EXCHANGE, BuiltinExchangeType.DIRECT);

		channel.queueDeclare(QUEUE, false, false, false, null);

		channel.queueBind(QUEUE, Producer.EXCHANGE, Producer.ROUTINGKEY);

		System.out.println("Consumer 等待接收消息 ..");

		DefaultConsumer consumer = new DefaultConsumer(channel){
			@Override														 // properties 由生产者发送时所带的属性值
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				
				String message = new String(body);
				
				System.out.println("接收到消息[routekey]" + envelope.getRoutingKey() + "=" + message);
				System.out.println("replyTo：" + properties.getReplyTo() + " correlationId：" + properties.getMessageId());
				
				// 将消息重发回这个队列 固定写法
				AMQP.BasicProperties replyProps = new AMQP.BasicProperties.
						Builder()
						.replyTo(properties.getReplyTo())
						.correlationId(properties.getMessageId())
						.build();

				channel.basicPublish("", replyProps.getReplyTo(), replyProps, ("回复：" + message).getBytes());
          
			}
		};
		channel.basicConsume(QUEUE, true, consumer);
	}

}
