package io.github.pleuvoir.delay;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.model.DelayMessage;
import io.github.pleuvoir.rabbitmq.producer.DelayMessageProducer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DelayMessageExampleTests {

	@Autowired
	private DelayMessageProducer delayMessageProducer;
	
	
	int num = 1;
	
	CountDownLatch countDownLatch = new CountDownLatch(num);

	@Test
	public void contextLoads() throws InterruptedException { 
		
		DelayMessage msg = new DelayMessage();
		msg.setId("1");
		msg.setBeginTime(LocalDateTime.now().plusSeconds(5));
		
		for (int i = 0; i < num; i++) {
			new Thread(new ProducerThead(msg)).start();
			countDownLatch.countDown();
		}
		
		TimeUnit.SECONDS.sleep(60);
	}
	
	
	public class ProducerThead implements Runnable {

		private DelayMessage msg;

		public ProducerThead(DelayMessage msg) {
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
			delayMessageProducer.send(msg);
		}
	}

}
