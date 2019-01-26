package io.github.pleuvoir.rabbit.creator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.github.pleuvoir.kit.ToJSON;

/**
 * 定时队列
 * @author pleuvoir
 *
 */
@Component
@Scope("prototype")
public class FixedTimeQueue implements ApplicationContextAware {

	private static Logger logger = LoggerFactory.getLogger(FixedTimeQueue.class);

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		FixedTimeQueue.applicationContext = applicationContext;
	}

	/**
	 * 队列标识，非必须
	 */
	private String requestId;

	/**
	 * 队列的过期时间，必须
	 */
	private LocalDateTime fixedTime;

	/**
	 * 队列名称
	 */
	private String queue;

	/**
	 * 交换机名称
	 */
	private String exchange;

	/**
	 * 路由键名称
	 */
	private String routingKey;
	
	/**
	 * 死信队列路由键名称，必须
	 */
	private String deadLetterRoutingKey;
	
	/**
	 * 死信队列交换机名称，必须
	 */
	private String deadLetterExchange;
	
	/**
	 * 定时队列是否创建成功
	 */
	private boolean alive;
	
	private FixedTimeQueue() {
		this.queue 			= null;
		this.exchange	 	= null;
		this.routingKey		= null;
		this.requestId 		= UUID.randomUUID().toString();
	}

	private FixedTimeQueue(LocalDateTime fixedTime) {
		this();
		this.fixedTime = fixedTime;
	}

	/**
	 * 声明一个定时队列，该队列中应当只有一条待消费的消息
	 * @param fixedTime	队列过期的时间，即消息执行的时间
	 */
	public static FixedTimeQueue create(LocalDateTime fixedTime) {
		return new FixedTimeQueue(fixedTime);
	}

	public FixedTimeQueue requestId(String requestId) {
		this.requestId = requestId;
		return this;
	}
	
	public FixedTimeQueue deadLetterRoutingKey(String deadLetterRoutingKey) {
		this.deadLetterRoutingKey = deadLetterRoutingKey;
		return this;
	}
	
	public FixedTimeQueue deadLetterExchange(String deadLetterExchange) {
		this.deadLetterExchange = deadLetterExchange;
		return this;
	}

	/**
	 * 向  rabbit 注册一个队列，队列会在到期后自动删除
	 */
	public FixedTimeQueue commit() {
		
		if (StringUtils.isEmpty(this.deadLetterExchange) || StringUtils.isEmpty(this.deadLetterRoutingKey)) {
			throw new IllegalArgumentException("deadLetterExchange and deadLetterRoutingKey 必须设置");
		}

		long delayMillis = Duration.between(LocalDateTime.now(), this.fixedTime).toMillis();
		
		String formatedDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(this.fixedTime);
		if (delayMillis < 0) {
			if (logger.isWarnEnabled()) {
				logger.warn("【定时队列创建】定时时间  {} 小于当前时间，不进行队列创建操作。", formatedDateTime);
			}
			return this;
		}

		String namePrefix 		= this.requestId.concat("-").concat(formatedDateTime);
		String exchangeName 	= namePrefix.concat("-fixedTime-exchange");
		String queueName	 	= namePrefix.concat("-fixedTime-queue");
		String routingKeyName 	= namePrefix.concat("-fixedTime-routingKey");

		// 当队列过期后会自动删除交换机
		Exchange exchange = ExchangeBuilder.directExchange(exchangeName).autoDelete().durable(true).build();
		Queue queue = QueueBuilder.durable(queueName)
				.withArgument("x-dead-letter-exchange", this.deadLetterExchange)
				.withArgument("x-dead-letter-routing-key", this.deadLetterRoutingKey)
				.withArgument("x-message-ttl", delayMillis) 	// 消息过期时间
				.withArgument("x-expires", delayMillis + 5000) 	// 队列自动删除时间，加些时间否则来不及消费
				.build();
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKeyName).noargs();

		RabbitAdmin rabbitAdmin = applicationContext.getBean(RabbitAdmin.class);
		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareQueue(queue);
		rabbitAdmin.declareBinding(binding);
		
		this.exchange = exchangeName;
		this.queue = queueName;
		this.routingKey = routingKeyName;
		this.alive = true;
		return this;
	}
	
	
	/**
	 * 如果队列创建成功则发送消息，该方法应该在 {@link #commit()} 之后调用
	 * @param msg	发送消息体
	 */
	public void sendMessageIfAlive(ToJSON msg) {
		if (isAlive()) {
			RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
			rabbitTemplate.convertAndSend(this.exchange, this.routingKey, msg.toJSON());
		}
	}

	public String getRequestId() {
		return requestId;
	}

	public String getQueue() {
		return queue;
	}

	public String getExchange() {
		return exchange;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public LocalDateTime getFixedTime() {
		return fixedTime;
	}

	/**
	 * 是否创建成功，失败请不要发送消息。
	 */
	public boolean isAlive(){
		return alive;
	}
}
