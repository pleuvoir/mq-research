package io.github.pleuvoir.rabbit.consumer;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;


@Service
public class ManualACKNormalMessageConsumer implements ChannelAwareMessageListener{

	private static Logger logger = LoggerFactory.getLogger(ManualACKNormalMessageConsumer.class);

	
	//@RabbitHandler
	public void handler(@Payload String data, @Headers Map<String,Object> headers, Channel channel) throws IOException {
		logger.info("NormalMessageConsumer 已接收到消息，payload：{}", data);
		
		//消费者操作
        System.out.println("---------收到消息，开始消费---------");

        /**
         * Delivery Tag 用来标识信道中投递的消息。RabbitMQ 推送消息给 Consumer 时，会附带一个 Delivery Tag，
         * 以便 Consumer 可以在消息确认时告诉 RabbitMQ 到底是哪条消息被确认了。
         * RabbitMQ 保证在每个信道中，每条消息的 Delivery Tag 从 1 开始递增。
         */
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        /**
         *  multiple 取值为 false 时，表示通知 RabbitMQ 当前消息被确认
         *  如果为 true，则额外将比第一个参数指定的 delivery tag 小的消息一并确认
         */
        boolean multiple = false;

        //ACK,确认一条消息已经被消费   注意 改为手动，如果未设置（AUTO）或者设置为 AUTO 则有可能会被 spring 自动确认，此处的确认会 ERROR
        // factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); 
		channel.basicAck(deliveryTag, multiple);
        
	}


	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		System.out.println(111111111);
	}

}
