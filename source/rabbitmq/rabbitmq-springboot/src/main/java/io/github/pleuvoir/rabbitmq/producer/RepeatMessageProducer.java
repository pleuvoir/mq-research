package io.github.pleuvoir.rabbitmq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.model.NormalMessage;
import io.github.pleuvoir.rabbitmq.helper.ReliableRabbitPublishTemplate;

@Component
public class RepeatMessageProducer  {

	private static Logger logger = LoggerFactory.getLogger(RepeatMessageProducer.class);

	@Autowired
	@Qualifier("reliableRabbitTemplate")
	private ReliableRabbitPublishTemplate rabbitTemplate;

	public void send(NormalMessage data) {
		logger.info("发送 Repeat 消息，{}", data.toJSON());
		rabbitTemplate.convertAndSend(RabbitConst.Repeat.EXCHANGE, RabbitConst.Repeat.ROUTING_KEY, data.toJSON());
	}

}
