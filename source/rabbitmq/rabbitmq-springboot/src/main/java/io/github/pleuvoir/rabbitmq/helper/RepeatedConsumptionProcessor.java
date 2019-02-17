package io.github.pleuvoir.rabbitmq.helper;

import java.time.Duration;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * MQ 消息重复消费处理，注意重复消费在 RabbitMQ 中只有手动确认方式可能会触发
 * TODO 此时消息是 unacked 的，需要近一步处理
 * @author pleuvoir
 *
 */
public class RepeatedConsumptionProcessor implements MessagePostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepeatedConsumptionProcessor.class);

	@Resource(name = "redisTemplate")
	private RedisTemplate<String, Object> redisTemplate;
	
	@Override
	public Message postProcessMessage(Message message) throws AmqpException {
		MessageProperties messageProperties = message.getMessageProperties();
		String messageId = messageProperties.getMessageId();
		
		LOGGER.info("*[去重处理器] messageId={}", messageId);
		
		if (StringUtils.isBlank(messageId)) {
			LOGGER.warn("*[去重处理器] 发送方未携带有效 messageId，已忽略。");
			return message;
		}
		
		// TOOD 想办法丢弃消息，REJECT 或者 ack 给 broker
		boolean putIfExist;
		try {
			putIfExist = redisTemplate.opsForValue().setIfAbsent(messageId, "0", Duration.ofHours(1));
			if (putIfExist) {
				LOGGER.info("非重复消息");
				return message;
			}
		} catch (Throwable e) {
			LOGGER.error("redis 异常，丢弃消息", e);
			return null;
		}
		
		LOGGER.warn("重复消息忽略");
		return null;
	}

}
