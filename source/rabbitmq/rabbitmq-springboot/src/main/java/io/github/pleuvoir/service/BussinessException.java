package io.github.pleuvoir.service;

public class BussinessException extends Exception {

	private static final long serialVersionUID = -8354025522492260258L;

	private String msg;


	public BussinessException() {
	}

	public BussinessException(String message) {
		super(message);
	}

	public BussinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BussinessException(Throwable cause) {
		super(cause);
	}

	public BussinessException(String fmt, Object... msg) {
		super(String.format(fmt, msg));
	}


	public String getMsg() {
		return msg;
	}

}
