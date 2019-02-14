package io.github.pleuvoir.rabbit.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

/**
 * 此类只是方便前端页面方便调用而构造的，在实际的逻辑中，并不代表生产者
 * @author pleuvoir
 *
 */
@Service
public class HttpPostMockProducer implements RabbitMQProducer{

	static Logger logger = LoggerFactory.getLogger(HttpPostMockProducer.class);
	
	
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void send(String data) {
		data = "支付成功，请求通知";
		logger.info("发送 异步通知消息给消费者解耦，{}", data);
		rabbitTemplate.convertAndSend(RabbitConst.Notify.EXCHANGE, RabbitConst.Notify.ROUTING_KEY, data);
	}

}
