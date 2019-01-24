package io.github.pleuvoir.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.pleuvoir.helper.ProducerPublisherConfirm;
import io.github.pleuvoir.helper.ProducerReturnCallBack;

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

	@Autowired
	private ProducerPublisherConfirm producerPublisherConfirm;
	@Autowired
	private ProducerReturnCallBack producerReturnCallBack;

	
	
	/**
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
		
		// 开启生产者发布确认
		factory.setPublisherConfirms(true);
		factory.setPublisherReturns(true);
		return factory;
	}

	@Bean(name = "rabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory getRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(20);
		factory.setAcknowledgeMode(AcknowledgeMode.NONE);
		return factory;
	}

	/*
	 * 发布消息使用的模版，因而发送方的许多参数可以在这里设置
	 */
	@Bean(name = "rabbitTemplate")
	public RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMandatory(true); // 开启故障检测机制，路由失败会回调
		template.setConfirmCallback(producerPublisherConfirm); // 生产者发布确认
		template.setReturnCallback(producerReturnCallBack); // 故障检测模式
		return template;
	}

	@Bean(name = "rabbitAdmin")
	public RabbitAdmin getRabbitAdmin(RabbitTemplate rabbitTemplate) {
		return new RabbitAdmin(rabbitTemplate);
	}

}
