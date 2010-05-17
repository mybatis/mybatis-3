package org.apache.ibatis.migration;

import org.apache.ibatis.exceptions.IbatisException;

public class MigrationException extends IbatisException {

  public MigrationException() {
    super();
  }

  public MigrationException(String message) {
    super(message);
  }

  public MigrationException(String message, Throwable cause) {
    super(message, cause);
  }

  public MigrationException(Throwable cause) {
    super(cause);
  }
}
