/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;

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
