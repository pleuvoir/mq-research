package io.github.pleuvoir.service;

public class LiveException extends Exception {

	private static final long serialVersionUID = -8354025522492260258L;

	private String msg;


	public LiveException() {
	}

	public LiveException(String message) {
		super(message);
	}

	public LiveException(String message, Throwable cause) {
		super(message, cause);
	}

	public LiveException(Throwable cause) {
		super(cause);
	}

	public LiveException(String fmt, Object... msg) {
		super(String.format(fmt, msg));
	}


	public String getMsg() {
		return msg;
	}

}
