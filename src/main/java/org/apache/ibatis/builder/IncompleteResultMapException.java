package org.apache.ibatis.builder;

import org.apache.ibatis.builder.BuilderException;

public class IncompleteResultMapException extends BuilderException {
  private static final long serialVersionUID = 5710340627945568032L;
  public IncompleteResultMapException() {
		super();
	}
	public IncompleteResultMapException(String message, Throwable cause) {
		super(message, cause);
	}
	public IncompleteResultMapException(String message) {
		super(message);
	}
	public IncompleteResultMapException(Throwable cause) {
		super(cause);
	}
}
