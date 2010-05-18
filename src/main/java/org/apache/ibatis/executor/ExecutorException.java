package org.apache.ibatis.executor;

import org.apache.ibatis.exceptions.PersistenceException;

public class ExecutorException extends PersistenceException {

  public ExecutorException() {
    super();
  }

  public ExecutorException(String message) {
    super(message);
  }

  public ExecutorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExecutorException(Throwable cause) {
    super(cause);
  }

}
