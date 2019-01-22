package io.github.pleuvoir.consumerack;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
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
 * 手动确认 + 事务
 * @author pleuvoir
 *
 */
public class AckConsumerWithTransaction {

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
		channel.exchangeDeclare(Const.DIRECT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// #########
		//  上面的部分和生产者完全一样
		// ########
		
		// 声明一个队列，注意队列是在消费者端声明的
		String queueName = "focuserror";
		channel.queueDeclare(queueName, false, false, false, null);

		// 绑定，将队列和交换器通过路由键进行绑定 表示只关注 error 级别的日志消息
		String routekey = "error";
		channel.queueBind(queueName, Const.DIRECT_EXCHANGE_NAME, routekey);

		System.out.println("AckConsumerWithTransaction 等待接收消息 ........");
		System.out.println("AckConsumerWithTransaction 开启消费者事务 ........");
		channel.txSelect();
		
		// 声明了一个消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				try {
					String message = new String(body, "UTF-8");
					System.out.println("接受到路由键[" + envelope.getRoutingKey() + "]" + message);
					
					
					
					if (ThreadLocalRandom.current().nextBoolean()) {
						// 消息确认并回滚 以  事务回滚为准，此条消息会重新放回队列
						channel.basicAck(envelope.getDeliveryTag(), false);
						channel.txRollback();
						System.out.println("消息确认并回滚 以  事务回滚为准，此条消息会重新放回队列，查看控制台会有一条 unacked Message");
					}else {
						channel.basicAck(envelope.getDeliveryTag(), false);
						channel.txCommit();
						System.out.println("消息确认并提交");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					// 拒绝
					//channel.basicNack(deliveryTag, multiple, requeue);
				}
			}
		};
		// 消费者正式开始在指定队列上消费消息 ， false 代表手动 ack
		channel.basicConsume(queueName, false, consumer);
	}

}
