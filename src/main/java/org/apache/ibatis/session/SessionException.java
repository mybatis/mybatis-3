package org.apache.ibatis.session;

import org.apache.ibatis.exceptions.IbatisException;

public class SessionException extends IbatisException {

  public SessionException() {
    super();
  }

  public SessionException(String message) {
    super(message);
  }

  public SessionException(String message, Throwable cause) {
    super(message, cause);
  }

  public SessionException(Throwable cause) {
    super(cause);
  }
}
