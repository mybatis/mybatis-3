package org.apache.ibatis.binding;

import org.apache.ibatis.exceptions.PersistenceException;

public class BindingException extends PersistenceException {

  public BindingException() {
    super();
  }

  public BindingException(String message) {
    super(message);
  }

  public BindingException(String message, Throwable cause) {
    super(message, cause);
  }

  public BindingException(Throwable cause) {
    super(cause);
  }
}
