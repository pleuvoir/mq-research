package io.github.pleuvoir.rabbitmq.helper;

import io.github.pleuvoir.rabbitmq.helper.ReliableExcuteWithTransaction.RabbitConsumeCallBack;

public interface ExcuteWithTransaction {

	void actualExcute(RabbitConsumeCallBack callBack, String messageId) throws Exception;

}
