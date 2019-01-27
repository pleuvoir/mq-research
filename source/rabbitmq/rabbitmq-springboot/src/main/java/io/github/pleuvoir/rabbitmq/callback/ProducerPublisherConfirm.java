package io.github.pleuvoir.rabbitmq.callback;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.stereotype.Component;

/**
 * 生产者发布确认
 * @author pleuvoir
 *
 */
@Component
public class ProducerPublisherConfirm implements RabbitTemplate.ConfirmCallback {

	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
		if (ack) {
			System.out.println("发送者确认发送给 mq 成功");
		} else {
			// 当发送消息到不存在的交换机时会触发
			// 处理失败的消息
			System.out.println("发送者发送给mq失败，可以考虑重发，将信息入库定时任务重发 ..:" + cause);
		}
	}

}
