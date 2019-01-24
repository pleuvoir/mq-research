package io.github.pleuvoir.model.vo;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import io.github.pleuvoir.common.RspCode;
import io.github.pleuvoir.kit.ToJSON;

/**
 * 返回响应结果
 * @author abeir
 *
 */
public class ResultVO<T> implements ToJSON {

	private String code;		//返回码
	
	private String msg;		//返回消息

	private T data;			//返回数据

	public ResultVO() {
	}

	public ResultVO(RspCode rspCode) {
		this.code = rspCode.getCode();
		this.msg = rspCode.getMsg();
	}

	public ResultVO(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public ResultVO(RspCode rspCode, ObjectError objectError) {
		this.code = rspCode.getCode();
		this.msg = objectError.getDefaultMessage();
	}
	
	public ResultVO(RspCode rspCode, FieldError fieldError) {
		this.code = rspCode.getCode();
		this.msg = fieldError.getDefaultMessage();
	}
	
	public ResultVO<T> setRspCode(RspCode rspCode) {
		this.code = rspCode.getCode();
		this.msg = rspCode.getMsg();
		return this;
	}

	public String getCode() {
		return code;
	}

	public ResultVO<T> setCode(String code) {
		this.code = code;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public ResultVO<T> setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public T getData() {
		return data;
	}

	public ResultVO<T> setData(T data) {
		this.data = data;
		return this;
	}

	@Override
	public String toString() {
		return toJSON();
	}
}
