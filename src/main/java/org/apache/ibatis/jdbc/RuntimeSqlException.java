package org.apache.ibatis.jdbc;

public class RuntimeSqlException extends RuntimeException {

  public RuntimeSqlException() {
    super();
  }

  public RuntimeSqlException(String message) {
    super(message);
  }

  public RuntimeSqlException(String message, Throwable cause) {
    super(message, cause);
  }

  public RuntimeSqlException(Throwable cause) {
    super(cause);
  }

}
