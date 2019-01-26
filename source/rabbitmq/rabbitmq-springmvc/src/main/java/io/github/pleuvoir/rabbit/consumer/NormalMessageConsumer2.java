package io.github.pleuvoir.rabbit.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;

@RabbitListener(
bindings = @QueueBinding(
		value = @Queue(RabbitConst.Normal.QUEUE),
		exchange = @Exchange(RabbitConst.Normal.EXCHANGE),
		key = RabbitConst.Normal.ROUTING_KEY
)
)
@Component
public class NormalMessageConsumer2 implements MessageListener {

	private static Logger logger = LoggerFactory.getLogger(NormalMessageConsumer2.class);

	@Override
	public void onMessage(Message message) {
		logger.info("NormalMessageConsumer2已接收到消息，payload：{}", new String(message.getBody()));
	}

}
