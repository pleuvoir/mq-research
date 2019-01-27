package io.github.pleuvoir.rabbitmq.creator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * 定时队列，该类是线程安全的
 * @author pleuvoir
 *
 */
public class FixedTimeQueueHelper  {

	private static Logger logger = LoggerFactory.getLogger(FixedTimeQueueHelper.class);

	private final RabbitAdmin rabbitAdmin;
	
	public FixedTimeQueueHelper(RabbitAdmin rabbitAdmin) {
		this.rabbitAdmin = rabbitAdmin;
	}

	/**
	 * 创建临时队列并发送消息
	 * @param deadLetterExchange	死信交换机
	 * @param deadLetterRoutingKey 死信路由键
	 * @param requestId 队列标识
	 * @param fixedTime 消息过期时间
	 * @throws FixedTimeDeclareException 队列创建失败时抛出
	 */
	public String declare(@NonNull String deadLetterExchange, @NonNull String deadLetterRoutingKey,
			@NonNull String requestId, @NonNull LocalDateTime fixedTime) throws FixedTimeDeclareException {
		
		check(deadLetterExchange, deadLetterRoutingKey, requestId, fixedTime);

		long delayMillis = Duration.between(LocalDateTime.now(), fixedTime).toMillis();

		String formatedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(fixedTime);
		if (delayMillis < 0) {
			if (logger.isWarnEnabled()) {
				logger.warn("【定时队列创建】定时时间  {} 小于当前时间，不进行队列创建操作。", formatedDateTime);
			}
			throw new FixedTimeDeclareException("定时时间 小于当前时间，不进行队列创建操作");
		}
		
		String namePrefix 		= requestId.concat("-").concat(formatedDateTime);
		String exchangeName 	= namePrefix.concat("-fixedTime-exchange");
		String queueName	 	= namePrefix.concat("-fixedTime-queue");
		String routingKeyName 	= namePrefix.concat("-fixedTime-routingKey");

		// 当队列过期后会自动删除交换机
		Exchange exchange = ExchangeBuilder.directExchange(exchangeName).autoDelete().durable(true).build();
		Queue queue = QueueBuilder.durable(queueName)
				.withArgument("x-dead-letter-exchange", deadLetterExchange)
				.withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
				.withArgument("x-message-ttl", delayMillis) 	// 消息过期时间
				.withArgument("x-expires", delayMillis + 5000) 	// 队列自动删除时间，加些时间否则来不及消费
				.build();
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKeyName).noargs();

		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareBinding(binding);
	
		return queueName;
	}
	
	
	/**
	 * 创建临时队列并发送消息
	 * @param deadLetterExchange	死信交换机
	 * @param deadLetterRoutingKey 死信路由键
	 * @param requestId 队列标识
	 * @param fixedTime 消息过期时间
	 * @param message 消息内容
	 * @throws FixedTimeDeclareException 队列创建失败时抛出
	 */
	public void declareAndSend(@NonNull String deadLetterExchange, @NonNull String deadLetterRoutingKey,
			@NonNull String requestId, @NonNull LocalDateTime fixedTime, @NonNull String message)
			throws FixedTimeDeclareException {
		
		check(deadLetterExchange, deadLetterRoutingKey, requestId, fixedTime);

		long delayMillis = Duration.between(LocalDateTime.now(), fixedTime).toMillis();

		String formatedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(fixedTime);
		if (delayMillis < 0) {
			if (logger.isWarnEnabled()) {
				logger.warn("【定时队列创建】定时时间  {} 小于当前时间，不进行队列创建操作。", formatedDateTime);
			}
			throw new FixedTimeDeclareException("定时时间 小于当前时间，不进行队列创建操作");
		}
		
		String namePrefix 		= requestId.concat("-").concat(formatedDateTime);
		String exchangeName 	= namePrefix.concat("-fixedTime-exchange");
		String queueName	 	= namePrefix.concat("-fixedTime-queue");
		String routingKeyName 	= namePrefix.concat("-fixedTime-routingKey");

		// 当队列过期后会自动删除交换机
		Exchange exchange = ExchangeBuilder.directExchange(exchangeName).autoDelete().durable(true).build();
		Queue queue = QueueBuilder.durable(queueName)
				.withArgument("x-dead-letter-exchange", deadLetterExchange)
				.withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
				.withArgument("x-message-ttl", delayMillis) 	// 消息过期时间
				.withArgument("x-expires", delayMillis + 5000) 	// 队列自动删除时间，加些时间否则来不及消费
				.build();
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKeyName).noargs();

		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareBinding(binding);
		
		this.rabbitAdmin.getRabbitTemplate().convertAndSend(exchangeName, routingKeyName, message);
	}
	
	private void check(String deadLetterExchange, String deadLetterRoutingKey, String requestId,
			LocalDateTime fixedTime) {
		Assert.hasLength(deadLetterExchange, "deadLetterExchange 必须设置");
		Assert.hasLength(deadLetterRoutingKey, "deadLetterRoutingKey 必须设置");
		Assert.hasLength(requestId, "requestId 必须设置");
		Assert.notNull(fixedTime, "fixedTime 必须设置");
	}

}
