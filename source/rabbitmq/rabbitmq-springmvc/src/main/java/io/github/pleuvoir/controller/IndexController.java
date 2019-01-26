package io.github.pleuvoir.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import io.github.pleuvoir.rabbit.helper.RabbitMQProducer;

@Controller
public class IndexController {

	static Logger logger = LoggerFactory.getLogger(IndexController.class);

	@Autowired
	private ApplicationContext applicationContext;

	@RequestMapping("/index")
	public ModelAndView welcome() {
		return new ModelAndView("/index");
	}

	@RequestMapping("{name}")
	public @ResponseBody String nornal(@PathVariable String name, String message) {

		RabbitMQProducer rabbitMQProducer = applicationContext.getBean(name, RabbitMQProducer.class);

		rabbitMQProducer.send(message);

		return "请求成功";
	}

}
