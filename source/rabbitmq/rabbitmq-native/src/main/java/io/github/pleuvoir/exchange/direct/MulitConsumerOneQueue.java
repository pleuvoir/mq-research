package io.github.pleuvoir.exchange.direct;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 一个连接多个信道，每个信道多个消费者，同时消费同一个队列，则会表现出消息在消费者之间的轮询发送。
 * @author pleuvoir
 *
 */
public class MulitConsumerOneQueue {

	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();
		String queueName = "队列" + ThreadLocalRandom.current().nextInt(9999999);

		ExecutorService pool = Executors.newFixedThreadPool(100);
		// 一个 TCP 连接创建了五个信道，每个信道对应 2 个消费者
		for (int i = 0; i < 5; i++) {
			pool.execute(new ConsumerWorker(connection, queueName));
		}
	}

	public static class ConsumerWorker implements Runnable {
		private Connection connection;
		private String queueName;

		public ConsumerWorker(Connection connection, String queueName) {
			super();
			this.connection = connection;
			this.queueName = queueName;
		}

		@Override
		public void run() {
			try {
				// 创建信道
				Channel channel = connection.createChannel();
				channel.exchangeDeclare(Const.DIRECT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
				// 声明一个队列，如果队列已存在，不会重复创建
				channel.queueDeclare(queueName, false, false, true, null);
				// 只关注 info 的消息
				channel.queueBind(queueName, Const.DIRECT_EXCHANGE_NAME, "info");

				System.out.println(Thread.currentThread().getName() +" 等待接收消息 ........");

				DefaultConsumer consumerA = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
							byte[] body) throws IOException {

						System.out.println(Thread.currentThread().getName() + "consumerA 接受到路由键[" + envelope.getRoutingKey()
								+ "]" + new String(body, "UTF-8"));
					}
				};
				
				channel.basicConsume(queueName, true, consumerA);
				
				DefaultConsumer consumerB = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
							byte[] body) throws IOException {

						System.out.println(Thread.currentThread().getName() + "consumerB 接受到路由键[" + envelope.getRoutingKey()
								+ "]" + new String(body, "UTF-8"));
					}
				};
				
				channel.basicConsume(queueName, true, consumerB);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
