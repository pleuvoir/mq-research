package io.github.pleuvoir.system;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * RabbitMQ 日志消费监控 <p>
 * TOPIC 交换机为 amq.rabbitmq.log ， 路由键 info、 warning、error
 * @author pleuvoir
 *
 */
public class RabbitMQLogConsumer {

	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("39.105.110.40");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		Connection connection = connectionFactory.newConnection();

		Channel channel = connection.createChannel();
		
		String queueName = "rabbitMQAllLevelLog";
		
		// 排他队列，
		channel.queueDeclare(queueName, false, true, false, null);

		 // 该队列关注所有日志
		channel.queueBind(queueName, "amq.rabbitmq.log", "#");

		System.out.println("RabbitMQLogConsumer 等待接收消息 ........");

		// 声明了一个消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("日志监控[" + envelope.getRoutingKey() + "]： " + message);
			}
		};
		channel.basicConsume(queueName, true, consumer);
	}
}
