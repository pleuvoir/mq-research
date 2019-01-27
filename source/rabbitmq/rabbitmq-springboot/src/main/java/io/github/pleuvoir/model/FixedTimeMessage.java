package io.github.pleuvoir.model;

import java.time.LocalDateTime;

import com.alibaba.fastjson.annotation.JSONField;

import io.github.pleuvoir.kit.ToJSON;

/**
 * 定时消息
 * @author pleuvoir
 *
 */
public class FixedTimeMessage implements ToJSON {

	/**
	 * 编号
	 */
	private String id;
	
	private String payload;
	
	/**
	 * 定时时间
	 */
	@JSONField(serialize = true, format = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime excutetime;

	// getter and setter
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getExcutetime() {
		return excutetime;
	}

	public void setExcutetime(LocalDateTime excutetime) {
		this.excutetime = excutetime;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

}
