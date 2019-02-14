package io.github.pleuvoir.rabbit.producer;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

@Component
public class RateLimitProducer implements RabbitMQProducer {

	private static Logger logger = LoggerFactory.getLogger(RateLimitProducer.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	int count = 1000;
	CyclicBarrier cyclicBarrier = new CyclicBarrier(count);

	@Override
	public void send(String data) {
		logger.info("并发发送 1000 条消息，{}", data);
		
		for (int i = 0; i < count; i++) {
			new ExcuteThread().start();
		}
		
	}

	
	 class ExcuteThread extends Thread{
		 
		@Override
		public void run() {
			try {
				cyclicBarrier.await();
				rabbitTemplate.convertAndSend(RabbitConst.RateLimit.EXCHANGE, RabbitConst.RateLimit.ROUTING_KEY, "我来秒杀了。。");
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
	}
	
}
