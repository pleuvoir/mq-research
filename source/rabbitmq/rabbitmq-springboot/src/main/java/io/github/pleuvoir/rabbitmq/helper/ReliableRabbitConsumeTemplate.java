package io.github.pleuvoir.rabbitmq.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;

import io.github.pleuvoir.rabbitmq.helper.ReliableExcuteWithTransaction.RabbitConsumeCallBack;

public class ReliableRabbitConsumeTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReliableRabbitConsumeTemplate.class);
	
	@Autowired
	private ReliableExcuteWithTransaction reliableExcuteWithTransaction;

	private boolean defaultRequeueStrategy = false;
	
	/**
	 * 可靠消息处理模板
	 * <p>
	 * 正常时会确认应答 ack， 当出现业务异常或者其它不可预知的错误时，程序会向 MQ broker 发送 nack 应答
	 * <p>
	 * 
	 * @param callBack	待执行的业务操作，此业务操作将在数据库事务中执行
	 * @param message
	 * @param channel
	 * @throws Throwable 
	 */
	public void excute(RabbitConsumeCallBack callBack, Message message, Channel channel) throws Throwable {
		this.excute(callBack, defaultRequeueStrategy, message, channel);
	}
	
	/**
	 * 可靠消息处理模板
	 * <p>
	 * 正常时会确认应答 ack， 当出现业务异常或者其它不可预知的错误时，程序会向 MQ broker 发送 nack 应答，当 requeue = true 时，此消息会被重新投递到其它消费者；
	 * 切记 requeue 参数的设置取决于再次消费是否可以恢复正常，如果不能，有可能一直轮询投递。
	 * <p>
	 * 
	 * @param callBack	待执行的业务操作，此业务操作将在数据库事务中执行
	 * @param requeue	出现异常时是否重新投递该消息，使用不当会有死循环的可能
	 * @param message
	 * @param channel
	 * @throws Throwable 
	 */
	public void excute(RabbitConsumeCallBack callBack, boolean requeue, Message message, Channel channel) throws Throwable {
		
		MessageProperties messageProperties = message.getMessageProperties();
		String messageId = messageProperties.getMessageId();
		long deliveryTag = messageProperties.getDeliveryTag();
		
		try {
			reliableExcuteWithTransaction.actualExcute(callBack, messageId);
		} catch (Throwable e) {
			LOGGER.warn("*[messageId={}] 业务执行失败，已回滚。", messageId, e);
			channel.basicNack(deliveryTag, false, requeue);
			if (requeue) {
				LOGGER.info("*[messageId={}] 消息已拒绝，并重新投递给其他消费者。", messageId);
			} else {
				LOGGER.info("*[messageId={}] 消息已拒绝。", messageId);
			}
			throw e; // 回滚并应答完成后，将异常抛出，方便外层判断业务异常或者其它异常
		}
		channel.basicAck(deliveryTag, false);
	}

	
	// helper
	
	/**
	 * 记录异常
	 */
	public void logException(Throwable e, Message message) {
		LOGGER.warn("*[messageId={}] 消息处理失败，异常信息[{}]，消息内容 ：{}", message.getMessageProperties().getMessageId(),
				e.getMessage(), new String(message.getBody()));
	}
}
