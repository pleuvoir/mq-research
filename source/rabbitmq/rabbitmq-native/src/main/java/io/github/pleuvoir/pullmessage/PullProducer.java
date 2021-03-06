package io.github.pleuvoir.pullmessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.github.pleuvoir.kit.Const;

/**
 * 拉取消息生产者，主要是为了配合消费者
 * @author pleuvoir
 *
 */
public class PullProducer {

	public static void main(String[] args) throws IOException, TimeoutException {
		// 创建连接，该地址为阿里云服务器地址 已开放 guest 远程访问权限
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("39.105.110.40");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		Connection connection = connectionFactory.newConnection();

		// 创建信道
		Channel channel = connection.createChannel();
		// 创建交换器
		channel.exchangeDeclare(Const.PULL_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// 日志消息级别，作为路由键使用
		String[] serverities = { "error", "info", "warning" };
		for (int i = 0; i < 30; i++) {
			String severity = serverities[i % 3];
			String msg = "Hello rabbitmq" + (i + 1);
			// 发布消息，需要参数：交换器，路由键，其中以日志消息级别为路由键
			channel.basicPublish(Const.PULL_EXCHANGE_NAME, severity, null, msg.getBytes());
			System.out.println("拉取消息生产者已发送  " + severity + ":" + msg);
		}
		channel.close();
		connection.close();
	}
}
