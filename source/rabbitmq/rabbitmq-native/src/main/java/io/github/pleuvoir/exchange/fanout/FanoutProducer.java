package io.github.pleuvoir.exchange.fanout;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.github.pleuvoir.exchange.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 普通生产者，使用 faunout 类型交换器（忽略路由键，类似于广播模式）
 * @author pleuvoir
 *
 */
public class FanoutProducer {

	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();

		// 创建信道
		Channel channel = connection.createChannel();
		// 创建交换器
		channel.exchangeDeclare(Const.FANOUT_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

		// 日志消息级别，作为路由键使用
		String[] serverities = { "error", "info", "warning" };
		for (int i = 0; i < 3; i++) {
			String severity = serverities[i % 3];
			String msg = "Hello rabbitmq" + (i + 1);
			// 发布消息，需要参数：交换器，路由键，其中以日志消息级别为路由键
			channel.basicPublish(Const.FANOUT_EXCHANGE_NAME, severity, null, msg.getBytes());
			System.out.println("fanout 已发送 " + severity + ":" + msg);
		}
		channel.close();
		connection.close();
	}
}
