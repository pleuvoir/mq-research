package io.github.pleuvoir.rabbit.consumer;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


//配置监听的哪一个队列，同时在没有 queue和exchange的情况下会去创建并建立绑定关系，有了这个注解变会自动确认
@RabbitListener(
		bindings = @QueueBinding(
				value = @Queue(RabbitConst.Normal.QUEUE),
				exchange = @Exchange(RabbitConst.Normal.EXCHANGE),
				key = RabbitConst.Normal.ROUTING_KEY
		)
)

@Service
public class AutoACKNormalMessageConsumer {

	private static Logger logger = LoggerFactory.getLogger(AutoACKNormalMessageConsumer.class);
	
	@RabbitHandler
	public void handler(@Payload String data, @Headers Map<String,Object> headers, Channel channel) throws IOException {
		logger.info("AutoACKNormalMessageConsumer 已接收到消息，payload：{}", data);
	}


}
