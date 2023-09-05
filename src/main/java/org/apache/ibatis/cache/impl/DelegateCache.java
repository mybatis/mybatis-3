package org.apache.ibatis.cache.impl;

import org.apache.ibatis.cache.Cache;

public abstract class DelegateCache implements Cache {

  private final Cache delegate;

  public DelegateCache(Cache delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public void putObject(Object key, Object value) {
    delegate.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return delegate.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }
}
