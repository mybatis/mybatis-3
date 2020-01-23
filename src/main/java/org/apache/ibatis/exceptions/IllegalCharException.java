package org.apache.ibatis.exceptions;

/**
 * @author chengdu
 */
public class IllegalCharException extends PersistenceException {
  public IllegalCharException() {
  }

  public IllegalCharException(String message) {
    super(message);
  }

  public IllegalCharException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalCharException(Throwable cause) {
    super(cause);
  }
}
