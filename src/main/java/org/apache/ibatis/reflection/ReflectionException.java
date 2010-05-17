package org.apache.ibatis.reflection;

import org.apache.ibatis.exceptions.IbatisException;

public class ReflectionException extends IbatisException {

  public ReflectionException() {
    super();
  }

  public ReflectionException(String message) {
    super(message);
  }

  public ReflectionException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReflectionException(Throwable cause) {
    super(cause);
  }

}
