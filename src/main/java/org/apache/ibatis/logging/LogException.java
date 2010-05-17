package org.apache.ibatis.logging;

import org.apache.ibatis.exceptions.IbatisException;

public class LogException extends IbatisException {

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
