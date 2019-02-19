package io.github.pleuvoir.helper;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.rabbitmq.helper.ReliableRabbitConsumeTemplate;
import io.github.pleuvoir.service.LiveBeginService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitConsumeTemplateTest {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(RabbitConsumeTemplateTest.class);

	@Autowired ReliableRabbitConsumeTemplate rabbitConsumeTemplate;
	@Autowired LiveBeginService userAccService;
	
	@Test
	public void testTemplate() throws InterruptedException {
		String userId = String.valueOf(ThreadLocalRandom.current().nextInt(9999));
		String messageId = String.valueOf(ThreadLocalRandom.current().nextInt(9999));

		try {
			
//			this.rabbitConsumeTemplate.excute(new RabbitConsumeCallBack() {
//				@Override
//				public void doInTransaction() throws BussinessException  {
//					userAccService.update(userId);
//				}
//			}, messageId);
			
		} catch (Throwable e) {
			LOGGER.error("出现错误，可以考虑回滚或者重试 {}", e.getMessage());
		}
		
		//TimeUnit.SECONDS.sleep(60);
	}

}
