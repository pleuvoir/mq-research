package io.github.pleuvoir.producerconfirm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.rabbitmq.producer.ProducerWithConfirmAndReturnCallback;

/**
 * 生产者发送确认和故障检测（发布消息到不存在的路由键）
 * @author pleuvoir
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProducerWithConfirmAndReturnCallbackTest {

	@Autowired
	private ProducerWithConfirmAndReturnCallback producer;
	
	
	int num = 1;
	
	CountDownLatch countDownLatch = new CountDownLatch(num);

	@Test
	public void contextLoads() throws InterruptedException { 
		
		
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
			producer.send(msg);
		}
	}

}
