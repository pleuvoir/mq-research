package io.github.pleuvoir.consumerack;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
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
 * 消息拒绝，可通过设置参数控制是否重新进入队列
 * @author pleuvoir
 *
 */
public class RejectOrNackConsumer {

	// 单次拒绝计数器
	static int count = 0;
	// 批量拒绝计数器
	static int batchCount = 0;
	
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

		System.out.println("RejectOrNackConsumer 等待接收消息 ........");

		// 声明了一个消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				try {
					//String message = new String(body, "UTF-8");
					//System.out.println("接受到路由键[" + envelope.getRoutingKey() + "]" + message);
					
					throw new RuntimeException();
					
				} catch (Exception e) {
					
					// 前 5 条消息 单次拒绝
					if (count++ < 5) {
						// 单次拒绝 ，并重新投递
						channel.basicReject(envelope.getDeliveryTag(), false);
						System.out.println("当前已单次拒绝" + count + "条消息..");
					} else {
						System.out.println("当前积攒数" + ++batchCount);
						// 后面积攒  15 条后 一次拒绝
						if (batchCount == 15) {
							System.out.println("休眠 10 秒 便于控制台查看 待确认的消息数  开始批量拒绝");
							try {
								TimeUnit.SECONDS.sleep(10);
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
							// 拒绝
							channel.basicNack(envelope.getDeliveryTag(), true, false);
							System.out.println("批量拒绝完成");
						}
					}
				}
			}
		};
		// 消费者正式开始在指定队列上消费消息 ， false 代表手动 ack
		channel.basicConsume(queueName, false, consumer);
	}

}
