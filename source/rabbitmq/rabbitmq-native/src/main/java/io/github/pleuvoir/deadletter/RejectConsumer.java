package io.github.pleuvoir.deadletter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
 * 拒绝消费者
 * @author pleuvoir
 *
 */
public class RejectConsumer {

	public static void main(String[] argv) throws IOException, TimeoutException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(Const.DLX.BEGIN_EXCHANGE, BuiltinExchangeType.DIRECT);
		
		// 指定死信交换机和路由键
		Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("x-dead-letter-exchange", Const.DLX.BEGIN_ARRIAL_EXCHANGE);	// 此交换机和路由键 可以找到队列 也就可以找到没人处理或被拒绝的消息
		arguments.put("x-dead-letter-routing-key", Const.DLX.BEGIN_ARRIAL_ROUTEKEY);
		
		channel.queueDeclare(Const.DLX.BEGIN_QUEUE, false, false, false, arguments);

		channel.queueBind(Const.DLX.BEGIN_QUEUE, Const.DLX.BEGIN_EXCHANGE, Const.DLX.BEGIN_ROUTEKEY);

		System.out.println("RejectConsumer 等待接收消息 ........");
		
		final Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				
				System.out.println("拒绝：接受到路由键[" + envelope.getRoutingKey() + "]" + message);
				
				// 消息被拒绝，并且设置 requeue 参数为 false 才会变成死信
				channel.basicReject(envelope.getDeliveryTag(), false);
			}
		};
		channel.basicConsume(Const.DLX.BEGIN_QUEUE, false, consumer);
	}

}
