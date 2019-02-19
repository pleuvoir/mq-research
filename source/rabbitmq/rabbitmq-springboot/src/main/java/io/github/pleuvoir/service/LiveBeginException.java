package io.github.pleuvoir.service;

public class LiveBeginException extends LiveException {

	private static final long serialVersionUID = 3556021736065154944L;

	public LiveBeginException() {
	}


	public LiveBeginException(String message) {
		super(message);
	}

	public LiveBeginException(String message, Throwable cause) {
		super(message, cause);
	}

	public LiveBeginException(Throwable cause) {
		super(cause);
	}

	public LiveBeginException(String fmt, Object... msg) {
		super(fmt, msg);
	}
}
