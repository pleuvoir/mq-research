package io.github.pleuvoir.producerconfirm;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ReturnListener;

import io.github.pleuvoir.kit.Const;

/**
 * 发送方异步确认模式
 * @author pleuvoir
 *
 */
public class AsyncConfirmProducer {

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
		// 创建持久化交换器，当生产者先发送消息 消费者再上线 则可以接收到 以前的消息
		channel.exchangeDeclare(Const.PRODUCER_ASYNC_CONFIRM_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

	    // 启用发送者确认模式
        channel.confirmSelect();
        
        //  异步确认监听器
		channel.addConfirmListener(new ConfirmListener() {
			public void handleAck(long deliveryTag, boolean multiple) throws IOException {
				// deliveryTag 包含消息序列号 multiple 为 true 代表批量
				System.out.println("已确认 handleAck deliveryTag：" + deliveryTag + "，multiple：" + multiple);
			}

			public void handleNack(long deliveryTag, boolean multiple) throws IOException {
				System.out.println("已确认 handleNack deliveryTag：" + deliveryTag + "，multiple：" + multiple);
			}
		});
        
		// 路由确认
		channel.addReturnListener(new ReturnListener() {
			public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body);
				System.out.println("RabbitMq 路由失败：  " + routingKey + "." + message);
			}
		});
		 
		// 日志消息级别，作为路由键使用
		String[] serverities = {"error", "warning"};
		for (int i = 0; i < 100; i++) {
			String severity = serverities[i % 2];
			String msg = "Hello rabbitmq" + (i + 1);
			
			// 路由失败通知 
			channel.basicPublish(Const.PRODUCER_ASYNC_CONFIRM_EXCHANGE_NAME, severity, true,
					MessageProperties.PERSISTENT_BASIC, msg.getBytes());
			
			System.out.println("发送方异步确认模式生产者已发送 " + severity + "：" + msg);
		}
		
//		channel.close();
//		connection.close();
	}
}
