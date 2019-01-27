package io.github.pleuvoir.producerconfirm;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.rabbitmq.producer.NoExchangeProducer;

/**
 * 交换机、路由键都不存在（结果：NACKED，不会触发 mandatory）
 * @author pleuvoir
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NoExchangeProducerExampleTests {

	@Autowired
	private NoExchangeProducer noExchangeProducer;
	
	
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
			noExchangeProducer.send(msg);
		}
	}

}
