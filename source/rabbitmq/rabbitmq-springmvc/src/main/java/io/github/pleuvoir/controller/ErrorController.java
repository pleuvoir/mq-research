package io.github.pleuvoir.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.pleuvoir.common.RspCode;
import io.github.pleuvoir.model.vo.ResultVO;


@RestController
@RequestMapping("/error")
@SuppressWarnings("rawtypes")
public class ErrorController {
	
	@RequestMapping("/400")
	public ResultVO<?> error400() {
		return new ResultVO(RspCode.ERROR.getCode(), "参数格式错误");
	}

	@RequestMapping("/404")
	public ResultVO<?> error404() {
		return new ResultVO(RspCode.ERROR.getCode(), "未定义的接口");
	}
	
	@RequestMapping("/405")
	public ResultVO<?> error405() {
		return new ResultVO(RspCode.ERROR.getCode(), "不支持的请求方式");
	}
}
