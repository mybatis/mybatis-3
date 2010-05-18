package org.apache.ibatis.transaction;

import org.apache.ibatis.exceptions.PersistenceException;

public class TransactionException extends PersistenceException {

  public TransactionException() {
    super();
  }

  public TransactionException(String message) {
    super(message);
  }

  public TransactionException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransactionException(Throwable cause) {
    super(cause);
  }

}
