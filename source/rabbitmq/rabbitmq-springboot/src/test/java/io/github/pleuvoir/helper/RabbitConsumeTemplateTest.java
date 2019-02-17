package io.github.pleuvoir.helper;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.pleuvoir.rabbitmq.helper.RabbitConsumeTemplate;
import io.github.pleuvoir.rabbitmq.helper.RabbitConsumeTemplate.RabbitConsumeCallBack;

public class RabbitConsumeTemplateTest {

	private final static Logger LOGGER = LoggerFactory.getLogger(RabbitConsumeTemplateTest.class);
	
	public static void main(String[] args) {

		String messageId = String.valueOf(ThreadLocalRandom.current().nextInt(9999));

		RabbitConsumeTemplate rabbitConsumeTemplate = new RabbitConsumeTemplate();
		rabbitConsumeTemplate.excute(new RabbitConsumeCallBack<Void>() {
			@Override
			public Void doInTransaction() {
				LOGGER.info("update p_mer_pay ..");
				LOGGER.info("update user_acc ..");
				LOGGER.info("update user_acc_detail ..");
				return null;
			}
		}, messageId);
	}
}
