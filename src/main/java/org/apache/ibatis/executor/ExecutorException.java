package org.apache.ibatis.executor;

import org.apache.ibatis.exceptions.IbatisException;

public class ExecutorException extends IbatisException {

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
