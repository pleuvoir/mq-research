package io.github.pleuvoir.rabbit.consumer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
		containerFactory = "manualRateLimitRabbitListenerContainerFactory",
		bindings = @QueueBinding(
				value = @Queue(RabbitConst.RateLimit.QUEUE),
				exchange = @Exchange(RabbitConst.RateLimit.EXCHANGE),
				key = RabbitConst.RateLimit.ROUTING_KEY
		)
)

@Service
public class RateLimitConsumer {

	private static Logger logger = LoggerFactory.getLogger(RateLimitConsumer.class);

	public AtomicLong count = new AtomicLong(1);
	
	@RabbitHandler
	public void onMessage(String message, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag, Channel channel) {
		try {
			try {
				logger.info("RateLimitConsumer 接收到消息:" + message);
				
			//	if (count.getAndIncrement() % 20 == 0) {
					// 这里也可以批量确认，减少网络传输
					TimeUnit.MILLISECONDS.sleep(150);
					channel.basicAck(deliveryTag, false);
					//logger.info("处理完成，单个应答MQ服务");
			//	}
				
					
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
