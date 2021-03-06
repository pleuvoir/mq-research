package test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.github.pleuvoir.ActiveMqConfiguration;
import io.github.pleuvoir.delay.DelayMqProducer;
import io.github.pleuvoir.delay.OrderExp;
import io.github.pleuvoir.normal.queue.ProducerQueue;
import io.github.pleuvoir.normal.topic.ProducerTopic;
import io.github.pleuvoir.replyto.ProducerR;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ActiveMqConfiguration.class)
public class AmSpringbootApplicationTests {

	@Autowired
	private ProducerQueue producerQueue;
	@Autowired
	private ProducerR producerR;
	@Autowired
	private ProducerTopic producerTopic;
	@Autowired
	private DelayMqProducer delayMqProducer;

	// 测试普通 queue
	@Test
	public void testQueueNormal() throws InterruptedException {
		Destination destination = new ActiveMQQueue("springboot.queue");
		for (int i = 0; i < 10; i++) {
			producerQueue.sendMessage(destination, "NO:" + i + ";my name is queue deep!!!");
		}
		TimeUnit.SECONDS.sleep(5);
	}

	// 测试 topic
	@Test
	public void testTopicNormal() throws InterruptedException {
		Destination destination = new ActiveMQTopic("springboot.topic");
		for (int i = 0; i < 3; i++) {
			producerTopic.sendMessage(destination, "NO:" + i + ";my name is topic deep!!!");
		}
		TimeUnit.SECONDS.sleep(5);
	}

	// 测试 request-response 模式
	@Test
	public void testReplyTo() throws InterruptedException {
		Destination destination = new ActiveMQQueue("springboot.replyto.queue");
		for (int i = 0; i < 3; i++) {
			producerR.sendMessage(destination, "NO:" + i + ";my name is reply deep!!!");
		}
		TimeUnit.SECONDS.sleep(5);
	}
	
	// 测试 delay 模式
	@Test
	public void testDelay() throws InterruptedException {
		Random r = new Random();
		OrderExp orderExp;
		long expireTime = r.nextInt(3) + 5;// 订单的超时时长，单位秒
		orderExp = new OrderExp();
		String orderNo = "你好DD00_" + expireTime + "S";
		orderExp.setOrderNo(orderNo);
		orderExp.setOrderNote(orderNo);
		orderExp.setOrderStatus("0");
		delayMqProducer.orderDelay(orderExp, expireTime);
		TimeUnit.SECONDS.sleep(50);
	}

}
