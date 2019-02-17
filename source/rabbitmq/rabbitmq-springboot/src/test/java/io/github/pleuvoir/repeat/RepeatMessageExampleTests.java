package io.github.pleuvoir.repeat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.model.NormalMessage;
import io.github.pleuvoir.rabbitmq.producer.RepeatMessageProducer;

/**
 * 重复消息测试
 * @author pleuvoir
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RepeatMessageExampleTests {

	@Autowired
	private RepeatMessageProducer repeatMessageProducer;
	
	
	int num = 1;
	
	CountDownLatch countDownLatch = new CountDownLatch(num);

	@Test
	public void contextLoads() throws InterruptedException { 
		
		NormalMessage msg = new NormalMessage();
		msg.setPayload("重复消息内容");
		
		for (int i = 0; i < num; i++) {
			new Thread(new ProducerThead(msg)).start();
			countDownLatch.countDown();
		}
		// 先启动单元测试，当报错时启动 springboot 充当另一个消费者
		TimeUnit.SECONDS.sleep(60);
	}
	
	
	public class ProducerThead implements Runnable {

		private NormalMessage msg;

		public ProducerThead(NormalMessage msg) {
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
			repeatMessageProducer.send(msg);
		}
	}

}
