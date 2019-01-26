package io.github.pleuvoir;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.pleuvoir.rabbit.callback.ProducerPublisherConfirm;
import io.github.pleuvoir.rabbit.callback.ProducerReturnCallBack;
import io.github.pleuvoir.rabbit.consumer.NormalMessageConsumer4;

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
	@Autowired
	private NormalMessageConsumer4 normalMessageConsumer4;

	@Bean(name = "rabbitAdmin")
	public RabbitAdmin getRabbitAdmin(RabbitTemplate rabbitTemplate) {
		return new RabbitAdmin(rabbitTemplate);
	}
	
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

	/**
	 * 消费者注解  {@RabbitListener} 需要用到
	 */
	@Bean(name = "rabbitListenerContainerFactory")
	public SimpleRabbitListenerContainerFactory getRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMaxConcurrentConsumers(20);
		factory.setAcknowledgeMode(AcknowledgeMode.NONE);
		return factory;
	}
	
	
//	/*// ========= 消费者手动确认 ==============
	@Bean
    public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container= new SimpleMessageListenerContainer(connectionFactory);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueues(consumerAckQueue());
        container.setMessageListener(normalMessageConsumer4);
        return container;
    }
   
   // 声明后 rabbit 控制台可以查看到已经有了此队列
   @Bean("consumerAckQueue")
	public Queue consumerAckQueue() {
		return new Queue("consumerAckQueue", true);
	}
   
   // ========= 消费者手动确认 ==============
   
   
   
   // ========= 发送方相关设置==============
   
	/*
	 * 发布消息使用的模版，因而发送方的许多参数可以在这里设置
	 */
	@Bean(name = "rabbitTemplate")
	public RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMandatory(true); // 开启故障检测机制，路由失败会回调
		template.setConfirmCallback(producerPublisherConfirm); // 生产者发布确认
		template.setReturnCallback(producerReturnCallBack); // 失败通知
		return template;
	}
	// ========= 发送方相关设置==============

}
