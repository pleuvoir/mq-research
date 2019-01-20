package io.github.pleuvoir.producerconfirm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.github.pleuvoir.kit.Const;

/**
 * 生产者批量确认模式，只演示生产者所以无需消费者<br>
 * 可以看到，即使没有消费者 也是确认成功的，所以生产者确认模式和消费者是没有关系的
 * @author pleuvoir
 *
 */
public class BatchConfirmProducer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
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
		channel.exchangeDeclare(Const.PRODUCER_CONFIRM_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// ##################
	    // 启用发送者确认模式
        channel.confirmSelect();
        
		// 日志消息级别，作为路由键使用
		String[] serverities = { "error", "info", "warning" };
		for (int i = 0; i < 3; i++) {
			String severity = serverities[i % 3];
			String msg = "Hello rabbitmq" + (i + 1);
			// 发布消息，需要参数：交换器，路由键，其中以日志消息级别为路由键
			channel.basicPublish(Const.PRODUCER_CONFIRM_EXCHANGE_NAME, severity, null, msg.getBytes());
			System.out.println("生产者批量确认模式生产者已发送 " + severity + ":" + msg);
			
			// 批量确认，要死大家一起死
		    channel.waitForConfirmsOrDie();
		}
		channel.close();
		connection.close();
	}
}
