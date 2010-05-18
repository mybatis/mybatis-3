package org.apache.ibatis.cache;

import org.apache.ibatis.exceptions.PersistenceException;

public class CacheException extends PersistenceException {

  public CacheException() {
    super();
  }

  public CacheException(String message) {
    super(message);
  }

  public CacheException(String message, Throwable cause) {
    super(message, cause);
  }

  public CacheException(Throwable cause) {
    super(cause);
  }

}
