package io.github.pleuvoir.rabbitmq.helper;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import io.github.pleuvoir.redis.RabbitMessageLogCache;
import io.github.pleuvoir.redis.RabbitMessageStatusEnum;

@Service
public class ReliableExcuteWithTransaction implements ExcuteWithTransaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReliableExcuteWithTransaction.class);
	
	@Autowired KeyValueTemplate keyValueTemplate;
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	@Override
	public void actualExcute(RabbitConsumeCallBack callBack, String messageId) throws Throwable {
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
