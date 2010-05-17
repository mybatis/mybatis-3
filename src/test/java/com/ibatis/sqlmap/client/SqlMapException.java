package com.ibatis.sqlmap.client;


/**
 * Thrown to indicate a problem with SQL Map configuration or state.  Generally
 * if an SqlMapException is thrown, something is critically wronge and cannot
 * be corrected until a change to the configuration or the environment is made.
 * <p/>
 * Note: Generally this wouldn't be used to indicate that an SQL execution error
 * occurred (that's what SQLException is for).
 */
public class SqlMapException extends RuntimeException {

  /**
   * Simple constructor
   */
  public SqlMapException() {
  }

  /**
   * Constructor to create exception with a message
   *
   * @param msg A message to associate with the exception
   */
  public SqlMapException(String msg) {
    super(msg);
  }

  /**
   * Constructor to create exception to wrap another exception
   *
   * @param cause The real cause of the exception
   */
  public SqlMapException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor to create exception to wrap another exception and pass a message
   *
   * @param msg   The message
   * @param cause The real cause of the exception
   */
  public SqlMapException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
