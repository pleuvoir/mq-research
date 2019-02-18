package io.github.pleuvoir.rabbitmq.helper;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.rabbitmq.client.Channel;

import io.github.pleuvoir.redis.RabbitMessageLogCache;
import io.github.pleuvoir.redis.RabbitMessageStatusEnum;

public class RabbitConsumeTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(RabbitConsumeTemplate.class);
	
	@Autowired KeyValueTemplate keyValueTemplate;

	/**
	 * 可靠消息处理模板
	 * @param callBack	待执行的业务操作
	 * @param requeue	出现异常时是否重新投递该消息
	 * @param message
	 * @param channel
	 * @throws Throwable 
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public void excute(RabbitConsumeCallBack callBack, boolean requeue, Message message, Channel channel)
			throws Throwable {

		MessageProperties messageProperties = message.getMessageProperties();
		String messageId = messageProperties.getMessageId();
		long deliveryTag = messageProperties.getDeliveryTag();
		try {
			actualExcute(callBack, messageId);
		} catch (Throwable cause) {
			channel.basicNack(deliveryTag, false, requeue);
			LOGGER.info("*[messageId={}] 消息已拒绝，并重新投递给其他消费者", messageId);
			throw new RuntimeException(cause);
		}
		channel.basicAck(deliveryTag, false);
		LOGGER.info("*[messageId={}] 消息已应答", messageId);
	}

	private void actualExcute(RabbitConsumeCallBack callBack, String messageId) throws Throwable {
		Optional<RabbitMessageLogCache> rabbitMessageLogOptional = keyValueTemplate.findById(messageId, RabbitMessageLogCache.class);
		if (!rabbitMessageLogOptional.isPresent()) {
			LOGGER.warn("*[messageId={}] 缓存中未能获取消息日志，忽略此次消息消费。", messageId);
			return;
		}

		RabbitMessageLogCache prevMessageLogCache = rabbitMessageLogOptional.get();
		if (prevMessageLogCache.getMessageStatus().equals(RabbitMessageStatusEnum.CONSUMER_SUCCESS)) {
			LOGGER.warn("*[messageId={}] 消息日志表明，此消息已经消费成功，可能是应答时出现故障，此次消息被忽略。", messageId);
			return;
		}

		Assert.notNull(callBack, "rabbitConsumeCallBack 不能为空");

		callBack.doInTransaction();

		RabbitMessageLogCache rabbitMessageLogCache = new RabbitMessageLogCache();
		rabbitMessageLogCache.setMessageId(messageId);
		rabbitMessageLogCache.setMessageStatus(RabbitMessageStatusEnum.CONSUMER_SUCCESS);
		keyValueTemplate.update(rabbitMessageLogCache);
		
		LOGGER.info("*[messageId={}] 已更新消息日志为成功。", messageId);
	}

	@FunctionalInterface
	public interface RabbitConsumeCallBack {
		void doInTransaction() throws Throwable;
	}

}
