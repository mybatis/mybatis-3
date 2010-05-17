package com.ibatis.dao.client;

/**
 * General runtime exception thrown by the DAO framework.
 * <p/>
 * <p/>
 */
public class DaoException extends RuntimeException {

  public DaoException() {
  }

  public DaoException(String msg) {
    super(msg);
  }

  public DaoException(Throwable cause) {
    super(cause);
  }

  public DaoException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
