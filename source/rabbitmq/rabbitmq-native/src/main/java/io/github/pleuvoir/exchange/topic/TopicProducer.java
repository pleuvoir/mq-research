package io.github.pleuvoir.exchange.topic;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * topic 交换机生产者
 * <p>
 * 规定路由键规则 A.WECHAT.INFO B.ORDER.ERROR C.API.WARN 
 * A 代表机器 ， INFO 代表日志级别， WECHAT 代表模块信息
 * </p>
 * @author pleuvoir
 *
 */
public class TopicProducer {

	public static void main(String[] args) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();

		Channel channel = connection.createChannel();

		channel.exchangeDeclare(Const.TOPIC_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

		// 日志消息级别，作为路由键使用
		String[] machines = { "A", "B", "C" };
		String[] bussiness = { "WECHAT", "ORDER", "API" };
		String[] logLevel = { "ERROR", "INFO", "WARN" };

		for (String machine : machines) {
			for (String bussines : bussiness) {
				for (String level : logLevel) {
					String routingKey = machine.concat(".").concat(bussines).concat(".").concat(level);
					String msg = "我是来自路由键".concat(routingKey).concat("的消息！");
					channel.basicPublish(Const.TOPIC_EXCHANGE_NAME, routingKey, null, msg.getBytes());
					System.out.println("topic 已发送 = "  + msg);
				}
			}
		}
		channel.close();
		connection.close();
	}

}
