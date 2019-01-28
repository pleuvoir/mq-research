package io.github.pleuvoir.rabbitmq.consumer;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import io.github.pleuvoir.kit.RabbitConst;


// 使用一个队列监听所有发送到此交换机的消息
@RabbitListener(
		bindings = @QueueBinding(
				value = @Queue,
				exchange = @Exchange(value = RabbitConst.Topic.EXCHANGE, type = ExchangeTypes.TOPIC),
				key = "#"
		)
)

@Service
public class AAndBAllConsumer {

	private static Logger logger = LoggerFactory.getLogger(AAndBAllConsumer.class);
	
	@RabbitHandler
	public void handler(@Payload String data, @Headers Map<String,Object> headers, Channel channel) throws IOException {
		logger.info("AAndBAllConsumer 已接收到消息，payload：{}", data);
	}


}
