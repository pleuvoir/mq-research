package io.github.pleuvoir.common;
/**
 * 返回码
 * @author abeir
 *
 */
public enum RspCode {

	SUCCESS("SUCCESS", "成功"),
	
	ERROR("ERROR", "系统忙，请稍后重试"),
	
	FAIL("FAIL", "操作失败");

	
	private String code;
	private String msg;
	
	private RspCode(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMsg() {
		return msg;
	}
}
