package io.github.pleuvoir.rabbitmq.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class RabbitConsumeTemplate {

	private final Logger logger = LoggerFactory.getLogger(RabbitConsumeTemplate.class);
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
	public <T> T excute(RabbitConsumeCallBack<T> callBack, String messageId) {
		checkCallBack(callBack);
		T result = callBack.doInTransaction();
		logger.info("已经更新为消息处理成功，messageId={}", messageId);
		return result;
	}

	private <T> void checkCallBack(RabbitConsumeCallBack<T> callBack) {
		Assert.notNull(callBack, "callBack 不能为空");
	}

	@FunctionalInterface
	public interface RabbitConsumeCallBack<T> {
		T doInTransaction();
	}

}
