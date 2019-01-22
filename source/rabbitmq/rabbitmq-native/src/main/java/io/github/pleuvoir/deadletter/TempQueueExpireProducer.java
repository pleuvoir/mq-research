package io.github.pleuvoir.deadletter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

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
		
		String queueNameA = declareTempQueueAndSend(channel, 10, Const.DLX.BEGIN_ARRIAL_EXCHANGE, Const.DLX.BEGIN_ARRIAL_ROUTEKEY, "来了老弟Q");
		
		// 方便控制台查看
		TimeUnit.SECONDS.sleep(5);
		
		// 	什么是没使用？ 一定时间内没有Get操作发生
		//	没有Consumer连接在队列上
		//	特别的：就算一直有消息进入队列，也不算队列在被使用。

		// 强行删除一个队列，无视 是否正在使用 或者队列上有消息
		// com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete = channel.queueDelete(queueNameA);
		 
		// System.out.println(queueDelete.protocolMethodName().equals("queue.delete-ok"));
		
		
		System.out.println("准备删除队列：" + queueNameA + "，当队列不在使用并且无视队列上是否有消息，如果不满足将抛出异常并且删除失败");
		 // 当队列不在使用，无视队列上是否有消息
		Queue.DeleteOk queueDelete2;
		try {
			queueDelete2 = channel.queueDelete(queueNameA, true, false);
			System.out.println(queueDelete2);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.out.println("已删除队列：" + queueNameA);
		
		// 不会抛出异常，但不保证删除成功
	//	channel.queueDeleteNoWait(queueName, true, true);
		
		System.out.println("重新定义新的临时队列，过期时间为 15 秒");
		
		String queueNameB = declareTempQueueAndSend(channel, 15, Const.DLX.BEGIN_ARRIAL_EXCHANGE, Const.DLX.BEGIN_ARRIAL_ROUTEKEY, "来了老弟W");
		 
		System.out.println("已重新定义队列：" + queueNameB + " DLXConsumer将于 15 秒后收到消息 来了老弟W");
		System.in.read();
		
		channel.close();
		connection.close();
	}
	
	
	private static String declareTempQueueAndSend(Channel channel,long delaySeconds, String dlxExchange, String dlxRoutingkey
			,String msg) throws IOException {
		String randomNamePrefix = String.valueOf(ThreadLocalRandom.current().nextLong(10000));
		String exchangeName = randomNamePrefix.concat("-exchange");
		String queueName = randomNamePrefix.concat("-queue");
		String routekeyName = randomNamePrefix.concat("-routekey");
		// 设置消息过期时间  
		long delayMillis = Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(delaySeconds)).toMillis();
		Map<String, Object>  arguments = new HashMap<>();
		arguments.put("x-message-ttl", delayMillis); 	// 消息过期时间
		arguments.put("x-expires", delayMillis + 5000); 	// 队列自动删除时间
		arguments.put("x-dead-letter-exchange", dlxExchange);
		arguments.put("x-dead-letter-routing-key", dlxRoutingkey);
		// 持久化且自动删除
		channel.exchangeDeclare(exchangeName, BuiltinExchangeType.DIRECT, true, true, null);
		// 持久化且不会自动删除
		channel.queueDeclare(queueName, true, false, false, arguments);
		// 绑定
		channel.queueBind(queueName, exchangeName, routekeyName);
		// 发消息
		channel.basicPublish(exchangeName, routekeyName, null, msg.getBytes());
		return queueName;
	}
}
