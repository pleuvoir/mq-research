package io.github.pleuvoir.rabbitmq.helper;

import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueTemplate;

import io.github.pleuvoir.redis.RabbitMessageLogCache;
import io.github.pleuvoir.redis.RabbitMessageStatusEnum;

public class ReliableRabbitTemplate extends RabbitTemplate {

	private final static Logger LOGGER = LoggerFactory.getLogger(ReliableRabbitTemplate.class);
	
	@Autowired KeyValueTemplate keyValueTemplate;
	
	public ReliableRabbitTemplate() {
		super();
	}

	public ReliableRabbitTemplate(ConnectionFactory connectionFactory) {
		super(connectionFactory);
	}

	@PostConstruct
	void setup() {
		this.setBeforePublishPostProcessors(new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				MessageProperties messageProperties = message.getMessageProperties();
				String messageId = Generator.nextUUID();
				messageProperties.setMessageId(messageId);
				RabbitMessageLogCache rabbitMessageLogCache = new RabbitMessageLogCache();
				rabbitMessageLogCache.setMessageId(messageId);
				rabbitMessageLogCache.setCreateTime(LocalDateTime.now());
				rabbitMessageLogCache.setMessageStatus(RabbitMessageStatusEnum.PREPARE_TO_BROKER);
				keyValueTemplate.insert(rabbitMessageLogCache);
				
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("*[messageId={}] 准备发送消息到 MQ Broker", messageId);
				}
				return message;
			}
		});
	}
}
