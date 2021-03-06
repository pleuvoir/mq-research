package io.github.pleuvoir.deadletter;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;

import io.github.pleuvoir.kit.Const;
import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 死信生产者 演示消息被拒绝时的效果，测试时同时启动 DLXConsumer 和  RejectConsumer
 * @author pleuvoir
 *
 */
public class DeadLetterRejectProducer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		Connection connection = RabbitMQKit.createConnection();
		Channel channel = connection.createChannel();
		
		channel.exchangeDeclare(Const.DLX.BEGIN_EXCHANGE, BuiltinExchangeType.DIRECT);
		
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
		for (int i = 0; i < 10; i++) {
			String msg = "Hello rabbitmq" + (i + 1);
			channel.basicPublish(Const.DLX.BEGIN_EXCHANGE, Const.DLX.BEGIN_ROUTEKEY,true, null, msg.getBytes());
		}
		Thread.sleep(5000);
		channel.close();
		connection.close();
	}
}
