package io.github.pleuvoir.model;

import io.github.pleuvoir.kit.ToJSON;

public class NormalMessage implements ToJSON {

	private String payload;

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

}
