package io.github.pleuvoir.rabbit.producer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

@Component
public class DelayMessageProducer implements RabbitMQProducer{
	
	private static Logger logger = LoggerFactory.getLogger(DelayMessageProducer.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	
	@Override
	public void send(String data) {
		
		logger.info("【延迟消息生产者】准备发送消息，data：{}", data);

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime beginTime = LocalDateTime.now().plusSeconds(5);
		
		long delayMillis  = Duration.between(now, beginTime).toMillis();

		long delaySeconds = Duration.ofMillis(delayMillis).getSeconds();
		
		if (delayMillis < 0) {
			logger.warn("【延迟消息生产者】 警告：延迟时间计算有误，开始时间：{}", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(beginTime));
			return;
		}
		
		logger.info("消息理论在{}秒后到达战场", delaySeconds);
		
		rabbitTemplate.convertAndSend(RabbitConst.Begin.EXCHANGE, RabbitConst.Begin.ROUTING_KEY, data, m -> {
				m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
				return m;
		});
	}

}
