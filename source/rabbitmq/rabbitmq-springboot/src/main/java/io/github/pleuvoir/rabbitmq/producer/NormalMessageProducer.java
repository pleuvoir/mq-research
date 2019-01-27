package io.github.pleuvoir.rabbitmq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.model.NormalMessage;

@Component
public class NormalMessageProducer  {

	private static Logger logger = LoggerFactory.getLogger(NormalMessageProducer.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void send(NormalMessage data) {
		logger.info("发送 Direct 消息，{}", data.toJSON());
		rabbitTemplate.convertAndSend(RabbitConst.Normal.EXCHANGE, RabbitConst.Normal.ROUTING_KEY, data.toJSON());
	}

}
