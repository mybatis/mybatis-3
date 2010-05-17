package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

public class ScheduledCache implements Cache {

  private Cache delegate;
  protected long clearInterval;
  protected long lastClear;

  public ScheduledCache(Cache delegate) {
    this.delegate = delegate;
    this.clearInterval = 60 * 60 * 1000; // 1 hour
    this.lastClear = System.currentTimeMillis();
  }

  public void setClearInterval(long clearInterval) {
    this.clearInterval = clearInterval;
  }

  public String getId() {
    return delegate.getId();
  }

  public int getSize() {
    clearWhenStale();
    return delegate.getSize();
  }

  public void putObject(Object key, Object object) {
    clearWhenStale();
    delegate.putObject(key, object);
  }

  public Object getObject(Object key) {
    if (clearWhenStale()) {
      return null;
    } else {
      return delegate.getObject(key);
    }
  }

  public Object removeObject(Object key) {
    clearWhenStale();
    return delegate.removeObject(key);
  }

  public void clear() {
    lastClear = System.currentTimeMillis();
    delegate.clear();
  }

  public ReadWriteLock getReadWriteLock() {
    return delegate.getReadWriteLock();
  }

  public int hashCode() {
    return delegate.hashCode();
  }

  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  private boolean clearWhenStale() {
    if (System.currentTimeMillis() - lastClear > clearInterval) {
      clear();
      return true;
    }
    return false;
  }

}
