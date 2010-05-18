package org.apache.ibatis.type;

import org.apache.ibatis.exceptions.PersistenceException;

public class TypeException extends PersistenceException {

  public TypeException() {
    super();
  }

  public TypeException(String message) {
    super(message);
  }

  public TypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public TypeException(Throwable cause) {
    super(cause);
  }

}
