package io.github.pleuvoir.rabbitmq.creator;

public class FixedTimeDeclareException extends Exception {

	private static final long serialVersionUID = -985307020648296733L;

	public FixedTimeDeclareException() {
		super();
	}

	public FixedTimeDeclareException(String message, Throwable cause) {
		super(message, cause);
	}

	public FixedTimeDeclareException(String message) {
		super(message);
	}

	public FixedTimeDeclareException(Throwable cause) {
		super(cause);
	}

}
