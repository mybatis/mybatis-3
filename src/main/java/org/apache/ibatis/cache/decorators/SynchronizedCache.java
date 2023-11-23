/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;

/**
 * @author Clinton Begin
 */
public class SynchronizedCache implements Cache {

  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Cache delegate;

  public SynchronizedCache(Cache delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    readWriteLock.readLock().lock();
    try {
      return delegate.getSize();
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    readWriteLock.writeLock().lock();
    try {
      delegate.putObject(key, object);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public Object getObject(Object key) {
    readWriteLock.readLock().lock();
    try {
      return delegate.getObject(key);
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public Object removeObject(Object key) {
    readWriteLock.writeLock().lock();
    try {
      return delegate.removeObject(key);
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public void clear() {
    readWriteLock.writeLock().lock();
    try {
      delegate.clear();
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

}
