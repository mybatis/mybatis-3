package org.apache.ibatis.logging;

import org.apache.ibatis.exceptions.PersistenceException;

public class LogException extends PersistenceException {

  public LogException() {
    super();
  }

  public LogException(String message) {
    super(message);
  }

  public LogException(String message, Throwable cause) {
    super(message, cause);
  }

  public LogException(Throwable cause) {
    super(cause);
  }

}
