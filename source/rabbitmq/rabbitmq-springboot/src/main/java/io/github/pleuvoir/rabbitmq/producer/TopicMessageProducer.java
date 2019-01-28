package io.github.pleuvoir.rabbitmq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;

/**
 * topic 交换机生产者
 * <p>
 * 规定路由键规则 A.WECHAT B.ORDER 
 * A 代表机器 ， WECHAT 代表模块信息
 * </p>
 * @author pleuvoir
 *
 */
@Component
public class TopicMessageProducer  {

	private static Logger logger = LoggerFactory.getLogger(TopicMessageProducer.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void send(String data) {
		
		String machines[] = { "A", "B" };
		String bussiness[] = { "WECHAT", "ORDER" };
		
		for (String machine : machines) {
			
			for (String bussines : bussiness) {
				
				String routingKey  = machine.concat(".").concat(bussines);
				
				logger.info("发送 Topic 消息，路由键：{}，消息：{}", routingKey, data);
				
				rabbitTemplate.convertAndSend(RabbitConst.Topic.EXCHANGE, 
						routingKey, 
						"我是 ".concat(routingKey));
				
			}
		}
		
	}

}
