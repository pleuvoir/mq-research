package io.github.pleuvoir.qos;

import java.io.IOException;
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
 * QOS 消费者
 * @author pleuvoir
 *
 */
public class QosConsumer {

	 // 批量确认收到的消息数量
	static int batchcount = 0;
	// 单条确认收到的消息数量
	static int count = 0;
	
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
		channel.exchangeDeclare(Const.QOS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// #########
		//  上面的部分和生产者完全一样
		// ########
		
		// 声明一个队列，注意队列是在消费者端声明的
		String queueName = "focuserror";
		channel.queueDeclare(queueName, false, false, false, null);

		// 绑定，将队列和交换器通过路由键进行绑定 表示只关注 error 级别的日志消息
		String routekey = "error";
		channel.queueBind(queueName, Const.QOS_EXCHANGE_NAME, routekey);


		
		// 声明了单个确认消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
			//	System.out.println("单个确认接受到路由键[" + envelope.getRoutingKey() + "]" + message);
				
				count++;
				// 单个确认
				channel.basicAck(envelope.getDeliveryTag(), false);
				System.out.println("单条确认已处理的消息数：" + count);
			}
		};
		
		// 开启 qos，  150  表示 一次确认的条数， true 代表整个信道每次 150 ， false 是每个消费者一次 150 ，一般不会同时设置
		channel.basicQos(150, true);
		
		channel.basicConsume(queueName, false, consumer);
		System.out.println("QosConsumer 单个确认等待接收消息 ........");
		
		
		// 声明了批量确认消费者
		final Consumer batchConsumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
			//	System.out.println("批量确认接受到路由键[" + envelope.getRoutingKey() + "]" + message);
				
				batchcount++;
				// 手动实现批量确认功能
				if (batchcount < 25) {
					System.out.println("当前批量积攒的消息未超过 " + 25 + "暂时不确认.当前：" + count);
				} else {
					// 批量确认
					System.out.println("走着..批量理论处理了 " + count + "条消息");
					 channel.basicAck(envelope.getDeliveryTag(), true);
				}
			}
		};
		
		channel.basicConsume(queueName, false, batchConsumer);
		System.out.println("QosConsumer 批量确认等待接收消息 ........");
	}

}
