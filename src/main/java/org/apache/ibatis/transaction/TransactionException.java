package org.apache.ibatis.transaction;

import org.apache.ibatis.exceptions.IbatisException;

public class TransactionException extends IbatisException {

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
