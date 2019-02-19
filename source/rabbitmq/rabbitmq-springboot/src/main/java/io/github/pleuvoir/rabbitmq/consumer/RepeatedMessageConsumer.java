package io.github.pleuvoir.rabbitmq.consumer;

import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbitmq.helper.ReliableRabbitConsumeTemplate;
import io.github.pleuvoir.service.LiveBeginException;
import io.github.pleuvoir.service.LiveBeginService;
import io.github.pleuvoir.service.LiveNotBeginException;


@RabbitListener(containerFactory = "manualRabbitListenerContainerFactory", 
		bindings = @QueueBinding(value = @Queue(RabbitConst.Repeat.QUEUE), 
					exchange = @Exchange(RabbitConst.Repeat.EXCHANGE), 
					key = RabbitConst.Repeat.ROUTING_KEY))

@Service
public class RepeatedMessageConsumer {

	private static Logger logger = LoggerFactory.getLogger(RepeatedMessageConsumer.class);

	@Autowired
	ReliableRabbitConsumeTemplate rabbitConsumeTemplate;
	@Autowired
	LiveBeginService liveBeginService;

	@RabbitHandler
	public void onMessage(String payload, Message message, Channel channel) throws Throwable    {

		logger.info("接收到消息：{}", payload);
	//	try {
//			rabbitConsumeTemplate.excute(new RabbitConsumeCallBack() {
//				@Override
//				public void doInTransaction() throws LiveBeginException, LiveNotBeginException {
//					liveBeginService.update(String.valueOf(ThreadLocalRandom.current().nextInt(9999)));
//				}
//			}, message, channel);

	//	} catch (Throwable e) {
			
		//	rabbitConsumeTemplate.ignoreExceptionAndLog(e, message);
//
//			if (e instanceof LiveBeginException) {
//				return;
//			} else if (e instanceof LiveNotBeginException) {
//				mqConsumeTimeFaultTolerant();
//			} else {
//				e.printStackTrace();
//				throw e;
//			}
	//	}
		
		try {
			rabbitConsumeTemplate.excute(() -> {
				liveBeginService.update(String.valueOf(ThreadLocalRandom.current().nextInt(9999)));
			}, message, channel);

		} catch (Throwable e) {
			rabbitConsumeTemplate.logException(e, message);
			if (e instanceof LiveBeginException) {
				return;
			} else if (e instanceof LiveNotBeginException) {
				mqConsumeTimeFaultTolerant();
			}
		}
	}

	private void mqConsumeTimeFaultTolerant() {
		logger.info("重试。。");
	}

	
	
	
//	MessageProperties messageProperties = message.getMessageProperties();
//	String messageId = messageProperties.getMessageId();
//	long deliveryTag = messageProperties.getDeliveryTag();
//	
//	//logger.info("接收到消息：{}，messageId={}", payload, messageId);
//
//	try {
//
//		rabbitConsumeTemplate.excute(() -> {
//			userAccService.update(String.valueOf(ThreadLocalRandom.current().nextInt(9999)));
//		}, false, message, channel);
//
//		if(ThreadLocalRandom.current().nextBoolean()) {
//			// 应答此消息
//			channel.basicAck(deliveryTag, false);
//			logger.info("*[messageId={}] 消息已应答", messageId);
//		}else {
//			logger.error("*[messageId={}] 模拟服务挂掉，消息未应答此时会出现重复消费", messageId);
//		}
//	} catch (Throwable e) {
//		 channel.basicNack(deliveryTag, false, false);
//		// 如果 redis 报错，到底是更新为消费成功还是没有？
//		// 出现任何异常都会回滚业务数据（redis 操作是否会受事务控制待定，但如果使用 db 表则一定会在一个事务中同时回滚）
//		// 此时当消费者下线后会 broker 会投递给其他消费者
//		logger.error("*[messageId={}]出现错误，可以考虑重试，注意：如果重试一直出错则会无限循环 {}", messageId, e.getMessage());
//	}
}
