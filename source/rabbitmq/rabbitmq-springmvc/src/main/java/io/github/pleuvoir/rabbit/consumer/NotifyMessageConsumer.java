package io.github.pleuvoir.rabbit.consumer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.rabbit.creator.fixedtime.FixedTimeDeclareException;
import io.github.pleuvoir.rabbit.creator.fixedtime.FixedTimeQueueHelper;


@RabbitListener(
		bindings = @QueueBinding(
				value = @Queue(RabbitConst.Notify.QUEUE),
				exchange = @Exchange(RabbitConst.Notify.EXCHANGE),
				key = RabbitConst.Notify.ROUTING_KEY
		)
)

@Service
public class NotifyMessageConsumer {

	private static Logger logger = LoggerFactory.getLogger(NotifyMessageConsumer.class);
	
	@Autowired
	private FixedTimeQueueHelper fixedTimeQueueHelper;
	
	AtomicLong count = new AtomicLong(0);
	
	// 延迟时间，3s 5s 8s 15s 30s
	String[] delaylevel = new String[] { "3", "5", "8", "15", "30" };
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RabbitHandler
	public void handler(@Payload String data) throws IOException {
		
		// 如果总次数超过 5 次 不再通知
		
		if (count.get() >= 5) {
			logger.info("总次数超过 5 次 不再通知");
			return;
		}
		
		logger.info("接收到消息或者定时队列时间到达，data：{}", data);
		
		logger.info("开始通知下游支付成功，时间：{}", LocalDateTime.now());
		ResponseEntity<String> response = restTemplate.getForEntity("http://127.0.0.1:88/rabbitmq-springmvc/notify", String.class);
		if(response.getStatusCode().is2xxSuccessful()){
			String body = response.getBody();
			logger.info("接收到下游响应：{}", body);
			if (StringUtils.equalsAnyIgnoreCase("success", body)) {
				logger.info("ok =========== 下游处理成功，终止通知");
			} else {
				logger.warn("ok =========== 下游应答处理失败，阶梯通知开始");
				// 创建定时队列
				buildNotifyqueueAndNotify();
			}
		}else{
			logger.error("Http 通知下游失败。");
		}
	}
	
	
	private void buildNotifyqueueAndNotify() {
		long seconds = Long.valueOf(delaylevel[(int) this.count.getAndIncrement()]);
		logger.info("定时队列第{}次发送通知，延迟{}秒", this.count.get(), seconds);
		try {
			fixedTimeQueueHelper.declareAndSend(RabbitConst.Notify.EXCHANGE, RabbitConst.Notify.ROUTING_KEY, 
					"一个有意义的编号", 
					LocalDateTime.now().plusSeconds(seconds), 
					"支付成功了，这是定时队列第" + this.count + "次通知");
		} catch (FixedTimeDeclareException e) {
			logger.warn("定时队列创建失败，{}", e.getMessage());
		}
	}

}
