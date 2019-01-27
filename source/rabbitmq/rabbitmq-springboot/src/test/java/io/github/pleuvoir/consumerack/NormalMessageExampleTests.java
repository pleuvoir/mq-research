package io.github.pleuvoir.consumerack;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.model.NormalMessage;
import io.github.pleuvoir.rabbitmq.producer.NormalMessageProducer;

/**
 * 发送 Direct 消息（同时测试消费者自动应答和手动应答）
 * @author pleuvoir
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NormalMessageExampleTests {

	@Autowired
	private NormalMessageProducer mormalMessageProducer;
	
	
	int num = 1;
	
	CountDownLatch countDownLatch = new CountDownLatch(num);

	@Test
	public void contextLoads() throws InterruptedException { 
		
		NormalMessage msg = new NormalMessage();
		msg.setPayload("1");
		
		for (int i = 0; i < num; i++) {
			new Thread(new ProducerThead(msg)).start();
			countDownLatch.countDown();
		}
		
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
			mormalMessageProducer.send(msg);
		}
	}

}
