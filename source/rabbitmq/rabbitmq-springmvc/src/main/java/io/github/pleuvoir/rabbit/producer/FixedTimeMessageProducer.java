package io.github.pleuvoir.rabbit.producer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbit.creator.fixedtime.FixedTimeDeclareException;
import io.github.pleuvoir.rabbit.creator.fixedtime.FixedTimeQueueHelper;
import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

@Component
public class FixedTimeMessageProducer implements RabbitMQProducer{
	
	private static Logger logger = LoggerFactory.getLogger(FixedTimeMessageProducer.class);

	@Autowired
	private FixedTimeQueueHelper fixedTimeQueueHelper;
	
	@Override
	public void send(String data) {
		
		logger.info("【定时消息生产者】准备发送消息，data：{}，消息将于 5 秒后被消费", data);
		try {
			fixedTimeQueueHelper.declareAndSend(RabbitConst.FixedTime.EXCHANGE, RabbitConst.FixedTime.ROUTING_KEY, 
					"一个有意义的编号", 
					LocalDateTime.now().plusSeconds(-1), 
					data);
		} catch (FixedTimeDeclareException e) {
			logger.warn("定时队列创建失败，{}", e.getMessage());
		}
	}

}
