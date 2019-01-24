package io.github.pleuvoir.model.dto;

import io.github.pleuvoir.kit.ToJSON;

public class NormalMessage implements ToJSON {
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
