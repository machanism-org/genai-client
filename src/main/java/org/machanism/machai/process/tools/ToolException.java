package org.machanism.machai.process.tools;

public class ToolException extends RuntimeException {

	public ToolException() {
		super();
	}

	public ToolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ToolException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -348437233342680075L;

	public ToolException(String message) {
		super(message);
	}

	public ToolException(String message, Throwable cause) {
		super(message, cause);
	}

}
