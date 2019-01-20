package io.github.pleuvoir.exchange.fanout;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 消费者，使用 fanout 类型交换器
 * @author pleuvoir
 *
 */
public class FanoutConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(Const.FANOUT_EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

		// 声明一个队列，关注 test 路由键
		String queueName = "fanout-测试队列";
		channel.queueDeclare(queueName, false, false, true, null);

		// 绑定，将队列和交换器通过路由键进行绑定 表示只关注 test 级别的日志消息，实际上发送者并未发送此路由键的消息
		String routekey = "test";
		channel.queueBind(queueName, Const.FANOUT_EXCHANGE_NAME, routekey);

		System.out.println("fanout 等待接收消息 ........");

		// 声明了一个消费者
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				System.out.println("接受到路由键[" + envelope.getRoutingKey() + "]" + message);
			}
		};
		// 消费者正式开始在指定队列上消费消息 ， true 代表自动 ack
		channel.basicConsume(queueName, true, consumer);
	}

}
