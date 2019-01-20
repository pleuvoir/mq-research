package io.github.pleuvoir.transaction;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 生产者发送消息时开启事务，不推荐使用，性能差
 * @author pleuvoir
 *
 */
public class TransactionProducer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.TRANSACTION_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
		String[] severities = { "error", "info", "warning" };
		// 开启事务
		channel.txSelect();
		try {
			for (int i = 0; i < 3; i++) {
				String severity = severities[i % 3];
				// 发送的消息
				String message = "Hello World_" + (i + 1);
				channel.basicPublish(Const.TRANSACTION_EXCHANGE_NAME, severity, true, null, message.getBytes());
				System.out.println("事务生产者发送消息: [" + severity + "]:'" + message + "'");
			}
			// 没有异常 提交
			channel.txCommit();
		} catch (Exception e) {
			e.printStackTrace();
			// 回滚
			channel.txRollback();
		}
		// 关闭频道和连接
		channel.close();
		connection.close();
	}

}
