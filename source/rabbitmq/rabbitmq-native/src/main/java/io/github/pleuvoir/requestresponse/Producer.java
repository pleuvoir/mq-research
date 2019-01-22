package io.github.pleuvoir.requestresponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.github.pleuvoir.kit.RabbitMQKit;

public class Producer {
	
	static String EXCHANGE = "RPC-EXCHANGE";
	static String ROUTINGKEY = "RPC-ROUTEKEY";
	
	public static void main(String[] args) throws IOException, TimeoutException {
		Channel channel = RabbitMQKit.createConnection().createChannel();
		
		channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT);
		
		// 响应 queue，消费者将会把要返回的信息发送到该 queue，注意这个队列没有绑定！
		String responseQueue = channel.queueDeclare().getQueue();
		String messageId = UUID.randomUUID().toString();
		
		System.out.println("响应queue名称：" + responseQueue + " 消息唯一id：" + messageId);
		
		BasicProperties props = new BasicProperties.Builder()
				.messageId(messageId)	 //消息的唯一id
				.replyTo(responseQueue)	// 响应的队列
				.build();
		
		System.out.println("Producer 等待接收消息 ..");
		DefaultConsumer consumer = new DefaultConsumer(channel){
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
					throws IOException {
				System.out.println("接收到消息[routekey]" + envelope.getRoutingKey() + "[" + new String(body) + "]");
			}
		};
		
		// 监听这个队列，消费者回会发
		channel.basicConsume(responseQueue, true, consumer);
		
		// 消息会发到 replyto 队列
		channel.basicPublish(EXCHANGE, ROUTINGKEY, props, "我是来自生产者的消息".getBytes());
	}

}
