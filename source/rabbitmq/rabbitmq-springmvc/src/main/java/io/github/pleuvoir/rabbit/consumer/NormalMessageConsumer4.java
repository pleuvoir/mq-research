package io.github.pleuvoir.rabbit.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

//@RabbitListener(
//		bindings = @QueueBinding(
//				value = @Queue(RabbitConst.Normal.QUEUE),
//				exchange = @Exchange(RabbitConst.Normal.EXCHANGE),
//				key = RabbitConst.Normal.ROUTING_KEY
//		)
//)

@Component
public class NormalMessageConsumer4 implements ChannelAwareMessageListener {

	private static Logger logger = LoggerFactory.getLogger(NormalMessageConsumer4.class);

	@RabbitHandler
	public void handler(String data) {

		logger.info("NormalMessageConsumer 已接收到消息，payload：{}", data);
	}

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		try {
			String data = new String(message.getBody());
			logger.info("NormalMessageConsumer4 已接收到消息，payload：{}", data);
			try {
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			} catch (Exception e) {
				logger.error("异常，NormalMessageConsumer4 拒绝消息，要求Mq重新派发", e);
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
				throw e;
			}
		} catch (Exception e) {
			logger.error("异常", e);
		}
	}

}
