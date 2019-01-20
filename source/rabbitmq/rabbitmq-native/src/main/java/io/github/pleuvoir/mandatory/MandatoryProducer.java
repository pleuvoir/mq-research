package io.github.pleuvoir.mandatory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

public class MandatoryProducer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.MANDATORY_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// 连接关闭时执行
		connection.addShutdownListener(new ShutdownListener() {
			public void shutdownCompleted(ShutdownSignalException cause) {
				System.out.println("连接关闭时执行：" + cause.getMessage());
			}
		});

		// 信道关闭时执行
		channel.addShutdownListener(new ShutdownListener() {
			public void shutdownCompleted(ShutdownSignalException cause) {
				System.out.println("信道关闭时执行：" + cause.getMessage());
			}
		});

		channel.addReturnListener(new ReturnListener() {
			public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body);
				System.out.println("返回的replyText ：" + replyText);
				System.out.println("返回的exchange ：" + exchange);
				System.out.println("返回的routingKey ：" + routingKey);
				System.out.println("返回的message ：" + message);
			}
		});

		String[] severities = { "error", "info", "warning" };
		for (int i = 0; i < 3; i++) {
			String severity = severities[i % 3];
			// 发送的消息
			String message = "Hello rabbitmq" + (i + 1);
			// 这里的 true 即代表开启  mandatory 模式
			channel.basicPublish(Const.MANDATORY_EXCHANGE_NAME, severity, true, null, message.getBytes());
			System.out.println("已发送 : [" + severity + "]:'" + message + "'");
		}
		Thread.sleep(3000);
		// 关闭频道和连接
		channel.close();
		connection.close();
	}

}
