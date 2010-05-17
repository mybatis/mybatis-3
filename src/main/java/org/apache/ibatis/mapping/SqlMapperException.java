package org.apache.ibatis.mapping;

import org.apache.ibatis.exceptions.IbatisException;

public class SqlMapperException extends IbatisException {

  public SqlMapperException() {
    super();
  }

  public SqlMapperException(String message) {
    super(message);
  }

  public SqlMapperException(String message, Throwable cause) {
    super(message, cause);
  }

  public SqlMapperException(Throwable cause) {
    super(cause);
  }

}
