package io.github.pleuvoir.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import io.github.pleuvoir.rabbitmq.creator.FixedTimeQueueHelper;

@Configuration
@EnableAutoConfiguration
public class RabbitConfiguration {

	@Bean(name = "rabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(20);
		factory.setAcknowledgeMode(AcknowledgeMode.NONE);
		return factory;
	}
	
	@Bean(name = "manualRabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory manualRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(20);
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
