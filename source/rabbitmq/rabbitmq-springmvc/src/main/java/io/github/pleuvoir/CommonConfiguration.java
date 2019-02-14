package io.github.pleuvoir;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.github.pleuvoir.rabbit.creator.fixedtime.FixedTimeQueueHelper;

@Configuration
@EnableRabbit
public class CommonConfiguration {

	@Value("${rabbitmq.host}")
	private String rabbitmqHost;
	@Value("${rabbitmq.port}")
	private Integer rabbitmqPort;
	@Value("${rabbitmq.virtualHost}")
	private String rabbitmqVirtualHost;
	@Value("${rabbitmq.username}")
	private String rabbitmqUsername;
	@Value("${rabbitmq.password}")
	private String rabbitmqPassword;

	
	/*
	 * 
	 *  // 原生 API 启用发送者确认模式
     *  // channel.confirmSelect();
     *  
	 */
	@Bean(name = "connectionFactory")
	public ConnectionFactory getConnectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setHost(rabbitmqHost);
		factory.setPort(rabbitmqPort == null ? 5672 : rabbitmqPort);
		factory.setVirtualHost(StringUtils.isBlank(rabbitmqVirtualHost) ? "/" : rabbitmqVirtualHost);
		if (StringUtils.isNotBlank(rabbitmqUsername)) {
			factory.setUsername(rabbitmqUsername);
		}
		if (StringUtils.isNotBlank(rabbitmqPassword)) {
			factory.setPassword(rabbitmqPassword);
		}
		factory.setPublisherConfirms(true);	// 开启生产者发布确认
		factory.setPublisherReturns(true);	// 开启路由失败回调
		return factory;
	}

	/**
	 * 自动确认监听工厂
	 * 消费者注解  {@RabbitListener} 需要用到，默认名称 是 rabbitListenerContainerFactory 注解也可以指定 RabbitListenerContainerFactory
	 */
	@Bean(name = "rabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory getRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(20);
		// 代码中现在使用自动监听的如果有3个 消费者，如果此处不设置那么默认为1，那么会为每个消费者创建一个信道，即创建 3个信道（一个信道一个消费者，原生 API 支持一个信道多个消费者）
		// 如果此处设置为 15 ，那么 会创建 45个信道， 应用程序层面的 45 个消费者
		factory.setConcurrentConsumers(2);  
		factory.setAcknowledgeMode(AcknowledgeMode.NONE); 
		return factory;
	}

	
	/**
	 * 手动确认监听工厂
	 */
	@Bean(name = "manualRabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory manualRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(20);
		factory.setConcurrentConsumers(1);  
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); 
		return factory;
	}
	
	/*
	 * 发布消息使用的模版，因而发送方的许多参数可以在这里设置<br>
     * 每一个消息情况不同，有的需要回调有的不需要，使用同一个会报错，故使用多例，如果项目采用者手动确认则必须如此设置
	 */
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Bean
	public RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		return template;
	}

	@Bean(name = "rabbitAdmin")
	public RabbitAdmin getRabbitAdmin(RabbitTemplate rabbitTemplate) {
		return new RabbitAdmin(rabbitTemplate);
	}
	
	// 临时队列小助手
	@Bean
	public FixedTimeQueueHelper fixedTimeQueueHelper(RabbitAdmin rabbitAdmin) {
		return new FixedTimeQueueHelper(rabbitAdmin);
	}

}
