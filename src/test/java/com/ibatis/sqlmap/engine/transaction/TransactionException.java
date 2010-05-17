package com.ibatis.sqlmap.engine.transaction;

import com.ibatis.sqlmap.client.SqlMapException;


public class TransactionException extends SqlMapException {

  public TransactionException() {
  }

  public TransactionException(String msg) {
    super(msg);
  }

  public TransactionException(Throwable cause) {
    super(cause);
  }

  public TransactionException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
