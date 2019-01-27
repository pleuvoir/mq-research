package io.github.pleuvoir.rabbit.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import io.github.pleuvoir.kit.RabbitConst;
import io.netty.util.internal.ThreadLocalRandom;


//配置监听的哪一个队列，同时在没有 queue和exchange的情况下会去创建并建立绑定关系
@RabbitListener(
		containerFactory = "manualRabbitListenerContainerFactory",
		bindings = @QueueBinding(
				value = @Queue(RabbitConst.Normal.QUEUE),
				exchange = @Exchange(RabbitConst.Normal.EXCHANGE),
				key = RabbitConst.Normal.ROUTING_KEY
		)
)

@Service
public class ManualACKNormalMessageConsumer {

	private static Logger logger = LoggerFactory.getLogger(ManualACKNormalMessageConsumer.class);
	
//	@RabbitHandler
//	public void handler(@Payload String data, @Headers Map<String,Object> headers, Channel channel) throws IOException {
//		logger.info("注解配合手动确认工厂 ManualACKNormalMessageConsumer 已接收到消息，payload：{}", data);
//	}

	
	@RabbitHandler
	public void onMessage(String message, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag, Channel channel) {
		try {
			try {
				logger.info("注解配合手动确认工厂 ManualACKNormalMessageConsumer 接收到消息:" + message);
				// 模拟异常
				if(ThreadLocalRandom.current().nextBoolean()){
					channel.basicAck(deliveryTag, false);
					logger.info("处理完成，应答MQ服务");
				}else{
					logger.info("处理失败，出现异常");
					throw new RuntimeException("出现异常");
				}
			} catch (Exception e) {
				channel.basicNack(deliveryTag, false, true);
				logger.warn("处理失败，拒绝消息，要求Mq重新派发", e);
				throw e;	// 让Throwable可以打印下日志
			}
		} catch (Throwable e) {
			logger.error(e.getMessage());
		}
	}
	
}
