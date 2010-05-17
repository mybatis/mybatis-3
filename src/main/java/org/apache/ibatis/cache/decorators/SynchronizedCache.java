package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;

import java.util.concurrent.locks.ReadWriteLock;

public class SynchronizedCache implements Cache {

  private Cache delegate;

  public SynchronizedCache(Cache delegate) {
    this.delegate = delegate;
  }

  public String getId() {
    return delegate.getId();
  }

  public int getSize() {
    acquireReadLock();
    try {
      return delegate.getSize();
    } finally {
      releaseReadLock();
    }
  }

  public void putObject(Object key, Object object) {
    acquireWriteLock();
    try {
      delegate.putObject(key, object);
    } finally {
      releaseWriteLock();
    }
  }

  public Object getObject(Object key) {
    acquireReadLock();
    try {
      return delegate.getObject(key);
    } finally {
      releaseReadLock();
    }
  }

  public Object removeObject(Object key) {
    acquireWriteLock();
    try {
      return delegate.removeObject(key);
    } finally {
      releaseWriteLock();
    }
  }


  public void clear() {
    acquireWriteLock();
    try {
      delegate.clear();
    } finally {
      releaseWriteLock();
    }
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

  private void acquireReadLock() {
    getReadWriteLock().readLock().lock();
  }

  private void releaseReadLock() {
    getReadWriteLock().readLock().unlock();
  }

  private void acquireWriteLock() {
    getReadWriteLock().writeLock().lock();
  }

  private void releaseWriteLock() {
    getReadWriteLock().writeLock().unlock();
  }

}
