package io.github.pleuvoir.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import io.github.pleuvoir.kit.RabbitConst;
import io.github.pleuvoir.model.dto.NormalMessage;
import io.github.pleuvoir.rabbit.producer.NormalMessageProducer;

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
	
	
	
	@RequestMapping("{type}")
	public @ResponseBody String nornal(@PathVariable String type, String message) {
		switch (type) {

		case "direct": {
			rabbitTemplate.convertAndSend(RabbitConst.Normal.EXCHANGE, RabbitConst.Normal.ROUTING_KEY,
					"一条来自 direct 交换机的消息");
			return "请求成功，请查看日志";
		}
		case "mandatoryWithNoneExchangeAndExistent": {
			
			rabbitTemplate.convertAndSend("不存在的交换机", RabbitConst.Normal.ROUTING_KEY, "不存在的交换机");
			return "请求成功，请查看日志（生产者 ACK 失败， 不会触发故障检测）";
		}
		case "mandatoryWithExchangeAndNonexistent": {
			
			rabbitTemplate.convertAndSend(RabbitConst.Normal.EXCHANGE, "不存在的路由键", "不存在的路由键");
			return "请求成功，请查看日志（生产者 ACK 成功， 同时触发故障检测）";
		}
		default:
			break;
		}
		return "请求成功";
	}
	
}
