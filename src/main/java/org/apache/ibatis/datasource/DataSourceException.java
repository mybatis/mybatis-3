package org.apache.ibatis.datasource;

import org.apache.ibatis.exceptions.PersistenceException;

public class DataSourceException extends PersistenceException {

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
