package io.github.pleuvoir.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 支付成功异步通知处理
 * @author pleuvoir
 *
 */
@Controller
public class NotifyController {

	static Logger logger = LoggerFactory.getLogger(NotifyController.class);

	AtomicLong count = new AtomicLong(1);
	
	// 模拟第五次时返回 success
	@RequestMapping("notify")
	public @ResponseBody String notifyFail() {
		long times = count.getAndIncrement();
		logger.info("下游接收到上游第" + times + "次通知");
		if (times % 10 == 0) {
			return "success";
		}
		return "fail";
	}
	
	
}
