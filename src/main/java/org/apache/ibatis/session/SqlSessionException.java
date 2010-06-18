package org.apache.ibatis.session;

import org.apache.ibatis.exceptions.PersistenceException;

public class SqlSessionException extends PersistenceException {

  public SqlSessionException() {
    super();
  }

  public SqlSessionException(String message) {
    super(message);
  }

  public SqlSessionException(String message, Throwable cause) {
    super(message, cause);
  }

  public SqlSessionException(Throwable cause) {
    super(cause);
  }
}
