package io.github.pleuvoir.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.model.dto.NormalMessage;
import io.github.pleuvoir.producer.NormalMessageProducer;

@Controller
public class IndexController {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Autowired
	private NormalMessageProducer normalMessageProducer;
	
	@RequestMapping("/index")
	public ModelAndView welcome() {
		return new ModelAndView("/index");
	}
	
	@RequestMapping("/direct")
	public @ResponseBody String nornal(String message) {
		NormalMessage normalMessage = new NormalMessage();
		normalMessage.setMessage(message);
		normalMessageProducer.send(normalMessage);
		
		// 发送一条不存在的
		rabbitTemplate.convertAndSend(RabbitConst.Normal.EXCHANGE + 1, RabbitConst.Normal.ROUTING_KEY, "不存在的交换机");
		return "Direct 交换机已发送消息";
	}
	
}
