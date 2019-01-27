package io.github.pleuvoir.rabbitmq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.model.FixedTimeMessage;
import io.github.pleuvoir.rabbitmq.creator.FixedTimeDeclareException;
import io.github.pleuvoir.rabbitmq.creator.FixedTimeQueueHelper;

@Component
public class FixedTimeMessageProducer  {
	
	private static Logger logger = LoggerFactory.getLogger(FixedTimeMessageProducer.class);

	@Autowired
	private FixedTimeQueueHelper fixedTimeQueueHelper;
	
	public void send(FixedTimeMessage data) {
		
		logger.info("【定时消息生产者】准备发送消息，data：{}", data.toJSON());
		try {
			fixedTimeQueueHelper.declareAndSend(RabbitConst.FixedTime.EXCHANGE, RabbitConst.FixedTime.ROUTING_KEY, 
					data.getId(), 
					data.getExcutetime(), 
					data.getPayload());
		} catch (FixedTimeDeclareException e) {
			logger.warn("定时队列创建失败，{}", e.getMessage());
		}
	}

}
