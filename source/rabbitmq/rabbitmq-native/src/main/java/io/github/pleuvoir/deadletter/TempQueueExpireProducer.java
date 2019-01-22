package io.github.pleuvoir.deadletter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 临时队列，临时交换机， 用来实现死信功能，测试时 需启动 DLXConsumer <p>
 * 此队列无消费者，过期后自动进入死信队列
 * @author pleuvoir
 *
 */
public class TempQueueExpireProducer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();
		
		String randomNamePrefix = String.valueOf(ThreadLocalRandom.current().nextLong(10000));
		
		String exchangeName = randomNamePrefix.concat("-exchange");
		String queueName = randomNamePrefix.concat("-queue");
		String routekeyName = randomNamePrefix.concat("-routekey");
		
		// 设置消息过期时间  5 秒
		long delayMillis = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(5)).toMillis();
		Map<String, Object>  arguments = new HashMap<>();
		arguments.put("x-message-ttl", delayMillis); 	// 消息过期时间
		arguments.put("x-expires", delayMillis + 5000); 	// 队列自动删除时间
		arguments.put("x-dead-letter-exchange", Const.DLX.BEGIN_ARRIAL_EXCHANGE);
		arguments.put("x-dead-letter-routing-key", Const.DLX.BEGIN_ARRIAL_ROUTEKEY);
		
		// 持久化且自动删除
		channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true, true, null);
		// 持久化且不会自动删除
		channel.queueDeclare(queueName, true, false, false, arguments);
		// 绑定
		channel.queueBind(queueName, exchangeName, routekeyName);
		
		// 如果路又失败，则会回调这里，此处只是为了排查 是否绑定了队列
		channel.addReturnListener(new ReturnListener() {
			public void handleReturn(int replyCode, String replyText, String exchange, String routingKey,
					AMQP.BasicProperties properties, byte[] body) throws IOException {
				
				StringBuffer sb = new StringBuffer();
				sb.append("路由失败：返回的replyText：" + replyText)
					.append("	返回的exchange：" + exchange)
					.append("	返回的routingKey：" + routingKey)
					.append("	返回的message：" + new String(body));
				System.out.println(sb.toString());
			}
		});
		
		
		for (int i = 1; i <= 10; i++) {
			String msg = "Hello rabbitmq" + i ;
			channel.basicPublish(exchangeName, routekeyName, true, null, msg.getBytes());
		}
		
		System.in.read();
		
		channel.close();
		connection.close();
	}
}
