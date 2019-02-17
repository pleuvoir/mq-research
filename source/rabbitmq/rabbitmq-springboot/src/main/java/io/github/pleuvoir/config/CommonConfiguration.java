package io.github.pleuvoir.config;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;

import io.github.pleuvoir.rabbitmq.creator.FixedTimeQueueHelper;
import io.github.pleuvoir.rabbitmq.helper.RepeatedConsumptionProcessor;

@EnableRedisRepositories(basePackages="io.github.pleuvoir.redis", repositoryImplementationPostfix = "Repository")
@Configuration
@AutoConfigureAfter({ RedisAutoConfiguration.class, RabbitAutoConfiguration.class })
public class CommonConfiguration {

	/**
	 *rabbit
	 */
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
		factory.setAfterReceivePostProcessors(repeatedConsumptionProcessor());
		return factory;
	}

	// 重复消息处理器
	// org.springframework.amqp.ImmediateAcknowledgeAmqpException: Message Post Processor returned 'null', discarding message
	@Bean(name = "repeatedConsumptionProcessor")
	public RepeatedConsumptionProcessor repeatedConsumptionProcessor() {
		return new RepeatedConsumptionProcessor();
	}
	
	/*
	 * 发布消息使用的模版，因而发送方的许多参数可以在这里设置<br>
	 * 每一个消息情况不同，有的需要回调有的不需要，使用同一个会报错，故使用多例，如果项目采用手动确认则必须如此设置
	 */
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Bean
	public RabbitTemplate getRabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setBeforePublishPostProcessors(new MessagePostProcessor() { // 携带消息 id
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				MessageProperties messageProperties = message.getMessageProperties();
				ThreadLocalRandom random = ThreadLocalRandom.current();
				UUID fastUUID = new UUID(random.nextLong(), random.nextLong());
				messageProperties.setMessageId(fastUUID.toString().replaceAll("-", ""));
				return message;
			}
		});
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
	
	

	/**
	 * redis
	 */
	@Bean("redisTemplate")
	public RedisTemplate<String,Object> getRedisTemplate(LettuceConnectionFactory redisConnectionFactory){
		RedisTemplate<String,Object> template = new RedisTemplate<>();
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericFastJsonRedisSerializer());
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean("stringRedisTemplate")
	public StringRedisTemplate getStringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}
	
}
