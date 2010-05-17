package org.apache.ibatis.datasource;

import org.apache.ibatis.exceptions.IbatisException;

public class DataSourceException extends IbatisException {

  public DataSourceException() {
    super();
  }

  public DataSourceException(String message) {
    super(message);
  }

  public DataSourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataSourceException(Throwable cause) {
    super(cause);
  }

}
