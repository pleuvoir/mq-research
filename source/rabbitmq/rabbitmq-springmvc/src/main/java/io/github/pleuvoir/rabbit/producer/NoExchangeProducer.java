package io.github.pleuvoir.rabbit.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

@Service
public class NoExchangeProducer
		implements RabbitMQProducer, RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

	private static Logger logger = LoggerFactory.getLogger(NoExchangeProducer.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	public void send(String data) {
		logger.info("交换机、路由键都不存在，{}", data);
		rabbitTemplate.setConfirmCallback(this);
		rabbitTemplate.setMandatory(true);
		rabbitTemplate.setReturnCallback(this);
		this.rabbitTemplate.convertAndSend("随便交换机", "随便路由键", "交换机、路由键都不存在");
	}

	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		if (ack) {
			// 如果confirm返回成功 则进行更新
			logger.info("交换机、路由键都不存在，ACKED（确认成功）");
		} else {
			// 失败则进行具体的后续操作:重试 或者补偿等手段
			logger.info("交换机、路由键都不存在，NACKED");
		}
	}

	@Override
	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
		StringBuffer detailMessage = new StringBuffer();
		detailMessage.append("\n   无法路由的消息，需要考虑另外处理。"  + "\n")
				.append(" Returned replyText：" + replyText + "\n")
				.append(" Returned exchange：" + exchange).append("Returned routingKey：" + routingKey + "\n")
				.append(" Returned Message：" + new String(message.getBody()));
		logger.warn("消息路由失败，详情： {}", detailMessage.toString());
	}

}
