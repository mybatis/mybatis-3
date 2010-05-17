package org.apache.ibatis.cache;

import org.apache.ibatis.exceptions.IbatisException;

public class CacheException extends IbatisException {

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
