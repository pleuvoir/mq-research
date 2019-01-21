package io.github.pleuvoir.qos;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.github.pleuvoir.kit.Const;

/**
 * QOS 生产者
 * @author pleuvoir
 *
 */
public class QosProducer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		// 创建连接，该地址为阿里云服务器地址 已开放 guest 远程访问权限
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("39.105.110.40");
		connectionFactory.setVirtualHost("/");
		connectionFactory.setUsername("guest");
		connectionFactory.setPassword("guest");
		Connection connection = connectionFactory.newConnection();

		// 创建信道
		Channel channel = connection.createChannel();
		// 创建交换器
		channel.exchangeDeclare(Const.QOS_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

		// 下面的逻辑 
		//	单条确认消费者共会收到  25 + 1 条消息
		//  批量确认消费者共会收到  24 + 1  条消息
		//
		String severity =  "error" ;
		for (int i = 0; i < 49; i++) {
			String msg = "Hello rabbitmq" + (i + 1);
			channel.basicPublish(Const.QOS_EXCHANGE_NAME, severity, null, msg.getBytes());
			System.out.println("QosProducer 已发送 " + severity + ":" + msg);
		}
		
		TimeUnit.SECONDS.sleep(10);
		
		channel.basicPublish(Const.QOS_EXCHANGE_NAME, severity, null, "这条消息应该会发往批量".getBytes());
		
		channel.basicPublish(Const.QOS_EXCHANGE_NAME, severity, null, "这条消息应该会发往单个".getBytes());
		
		channel.close();
		connection.close();
	}
}
