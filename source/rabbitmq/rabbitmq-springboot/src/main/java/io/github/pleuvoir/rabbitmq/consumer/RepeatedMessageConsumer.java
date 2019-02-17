package io.github.pleuvoir.rabbitmq.consumer;

import java.util.concurrent.ThreadLocalRandom;

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


@RabbitListener(
		containerFactory = "manualRabbitListenerContainerFactory",
		bindings = @QueueBinding(
				value = @Queue(RabbitConst.Repeat.QUEUE),
				exchange = @Exchange(RabbitConst.Repeat.EXCHANGE),
				key = RabbitConst.Repeat.ROUTING_KEY
		)
)

@Service
public class RepeatedMessageConsumer {

	private static Logger logger = LoggerFactory.getLogger(RepeatedMessageConsumer.class);
	
	
	@RabbitHandler
	public void onMessage(String message, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag, Channel channel) {
		
		logger.info("重复消息消费者 RepeatedMessageConsumer 接收到消息:" + message);
		
		try {
			try {
				// 模拟异常
//				if(ThreadLocalRandom.current().nextBoolean()){
//					channel.basicAck(deliveryTag, false);
//					logger.info("处理完成，应答MQ服务");
//				}else{
					//logger.info("处理失败，出现异常");
					throw new RuntimeException("出现异常");
			//	}
			} catch (Exception e) {
			//	channel.basicNack(deliveryTag, false, true);
				logger.warn("处理失败，请手动下线此消费者，不做任何处理，观察别的消费者");
				throw e;	// 让Throwable可以打印下日志
			}
		} catch (Throwable e) {
			//logger.error(e.getMessage());
		}
	}
	
}
