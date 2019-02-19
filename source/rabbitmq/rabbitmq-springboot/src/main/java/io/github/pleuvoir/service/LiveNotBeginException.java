package io.github.pleuvoir.service;

public class LiveNotBeginException extends LiveException {


	private static final long serialVersionUID = 1610307215756370799L;

	
	public LiveNotBeginException() {
	}


	public LiveNotBeginException(String message) {
		super(message);
	}

	public LiveNotBeginException(String message, Throwable cause) {
		super(message, cause);
	}

	public LiveNotBeginException(Throwable cause) {
		super(cause);
	}
	
	public LiveNotBeginException(String fmt, Object... msg) {
		super(fmt, msg);
	}
}
