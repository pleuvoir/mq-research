package io.github.pleuvoir.rabbit.callback;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 故障检测，需要设置 	mandatory = true <br>
 * 无法路由的消息将会打印
 * @author pleuvoir
 *
 */
@Component
public class ProducerReturnCallBack implements RabbitTemplate.ReturnCallback {

	@Override
	public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
		System.out.println("无法路由的消息，需要考虑另外处理。");
		System.out.println("Returned replyText：" + replyText);
		System.out.println("Returned exchange：" + exchange);
		System.out.println("Returned routingKey：" + routingKey);
		String msgJson = new String(message.getBody());
		System.out.println("Returned Message：" + msgJson);
	}

}
