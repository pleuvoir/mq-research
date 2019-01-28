package io.github.pleuvoir.topic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.model.NormalMessage;
import io.github.pleuvoir.rabbitmq.producer.TopicMessageProducer;


/**
 * topic 交换机生产者
 * <p>
 * 规定路由键规则 A.WECHAT B.ORDER 
 * A 代表机器 ， WECHAT 代表模块信息
 * </p>
 * @author pleuvoir
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TopicExampleTests {

	@Autowired
	private TopicMessageProducer topicMessageProducer;
	
	
	int num = 1;
	
	CountDownLatch countDownLatch = new CountDownLatch(num);

	@Test
	public void contextLoads() throws InterruptedException { 
		
		NormalMessage msg = new NormalMessage();
		msg.setPayload("1");
		
		for (int i = 0; i < num; i++) {
			new Thread(new ProducerThead(String.valueOf(i))).start();
			countDownLatch.countDown();
		}
		
		TimeUnit.SECONDS.sleep(60);
	}
	
	
	public class ProducerThead implements Runnable {

		private String msg;

		public ProducerThead(String msg) {
			super();
			this.msg = msg;
		}

		@Override
		public void run() {
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			topicMessageProducer.send(msg);
		}
	}

}
