package io.github.pleuvoir.queue;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import io.github.pleuvoir.kit.RabbitMQKit;

/**
 * 队列控制
 * @author pleuvoir
 *
 */
@SuppressWarnings("unused")
public class QueueControl {
	
	public static void main(String[] args) throws IOException, TimeoutException {
		
		Connection connection = RabbitMQKit.createConnection();
		
		Channel channel = connection.createChannel();
		
		// 队列名称，是否持久化（重启后也不会消失），
		// 是否消费者独占（通队列允许的消费者没有限制，多个消费者绑定到多个队列时，RabbitMQ会采用轮询进行投递。如果需要消费者独占队列，在队列创建的时候，设定属性exclusive为true。）
		// 是否自动删除（当所有消费者断开连接，也就是最后一个消费者断开连接）
		// 扩展参数，具体可查看文档
		//channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
		//channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);
		
		//arguments.put("x-expires", delayMillis + 5000); 	// 队列过期删除时间
	}
}
