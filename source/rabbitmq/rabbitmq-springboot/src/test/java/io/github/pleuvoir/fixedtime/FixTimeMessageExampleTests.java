package io.github.pleuvoir.fixedtime;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.model.FixedTimeMessage;
import io.github.pleuvoir.rabbitmq.producer.FixedTimeMessageProducer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FixTimeMessageExampleTests {

	@Autowired
	private FixedTimeMessageProducer fixedTimeMessageProducer;
	
	
	int num = 1;
	
	CountDownLatch countDownLatch = new CountDownLatch(num);

	@Test
	public void contextLoads() throws InterruptedException { 
		
		for (int i = 0; i < num; i++) {
			FixedTimeMessage msg = new FixedTimeMessage();
			msg.setId(String.valueOf(i));
			msg.setExcutetime(LocalDateTime.now().plusSeconds(10));
			msg.setPayload("hello :boom: " + (i + 1));
			new Thread(new ProducerThead(msg)).start();
			countDownLatch.countDown();
		}
		
		TimeUnit.SECONDS.sleep(60);
	}
	
	
	public class ProducerThead implements Runnable {

		private FixedTimeMessage msg;

		public ProducerThead(FixedTimeMessage msg) {
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
			fixedTimeMessageProducer.send(msg);
		}
	}

}
