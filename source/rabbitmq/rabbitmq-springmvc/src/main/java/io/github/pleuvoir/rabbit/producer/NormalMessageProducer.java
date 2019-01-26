package io.github.pleuvoir.rabbit.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

@Component
public class NormalMessageProducer implements RabbitMQProducer {

	private static Logger logger = LoggerFactory.getLogger(NormalMessageProducer.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void send(String data) {
		logger.info("发送 Direct 消息，{}", data);
		rabbitTemplate.convertAndSend(RabbitConst.Normal.EXCHANGE, RabbitConst.Normal.ROUTING_KEY, data);
	}

}
