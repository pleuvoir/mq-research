package io.github.pleuvoir.delay;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class DelayConsume  {

	@JmsListener(destination = "order.delay")
	public void onMessage(Message message) {
		String data;
		try {
			data = ((TextMessage) message).getText();
			System.out.println("接收到延迟消息。" + data);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
