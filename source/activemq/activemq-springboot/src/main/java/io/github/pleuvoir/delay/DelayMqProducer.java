package io.github.pleuvoir.delay;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.ScheduledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

@Service
public class DelayMqProducer {

	private Logger logger = LoggerFactory.getLogger(DelayMqProducer.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	/**
	 * 类说明：创建消息的类
	 */
	private static class CreateMessage implements MessageCreator {

		private OrderExp order;
		private long expireTime;

		public CreateMessage(OrderExp order, long expireTime) {
			super();
			this.order = order;
			this.expireTime = expireTime;
		}

		public Message createMessage(Session session) throws JMSException {
			Message message = session.createTextMessage(JSON.toJSONString(order));
			message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, expireTime);
			return message;
		}
	}

	public void orderDelay(OrderExp order, long expireTime) {
		logger.info("订单[超时时长：" + expireTime + "秒] 将被发送给消息队列，详情：" + order);
		jmsTemplate.send("order.delay", new CreateMessage(order, expireTime * 1000));
	}

}
