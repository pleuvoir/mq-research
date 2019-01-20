package io.github.pleuvoir.pullmessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import io.github.pleuvoir.kit.Const;

/**
 * 拉取消息消费者，采用拉的模式
 * @author pleuvoir
 *
 */
public class PullConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException, InterruptedException {
		// 创建连接，该地址为阿里云服务器地址 已开放 guest 远程访问权限
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("39.105.110.40");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		Connection connection = connectionFactory.newConnection();

		// 打开连接和创建频道，与发送端一样
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.PULL_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// #########
		//  上面的部分和生产者完全一样
		// ########
		
		// 声明一个队列，使用现成的也可以
		String queueName = "focuserror";
		channel.queueDeclare(queueName, false, false, false, null);

		// 绑定，将队列和交换器通过路由键进行绑定 表示只关注 error 级别的日志消息
		String routekey = "error";
		channel.queueBind(queueName, Const.PULL_EXCHANGE_NAME, routekey);

		System.out.println("拉取消息消费者等待接收消息 ........");

		
		// 1 秒拉一次
		for (;;) {
			GetResponse response = channel.basicGet(queueName, true);
			if (response != null) {
				System.out.println("接收到[" + response.getEnvelope().getRoutingKey() + "]" + new String(response.getBody()));
			}
			Thread.sleep(1000);
		}
		
		// 如下注释的部分是常规 push 型
		// 声明了一个消费者
//		final Consumer consumer = new DefaultConsumer(channel) {
//			@Override
//			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//				String message = new String(body, "UTF-8");
//				System.out.println("接受到路由键[" + envelope.getRoutingKey() + "]" + message);
//			}
//		};
//		channel.basicConsume(queueName, true, consumer);
	}

}
