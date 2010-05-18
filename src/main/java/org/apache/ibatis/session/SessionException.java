package org.apache.ibatis.session;

import org.apache.ibatis.exceptions.PersistenceException;

public class SessionException extends PersistenceException {

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
