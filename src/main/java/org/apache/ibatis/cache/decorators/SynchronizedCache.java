/*
 *    Copyright 2009-2024 the original author or authors.
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

import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheDecorator;

/**
 * @author Clinton Begin
 */
public class SynchronizedCache extends CacheDecorator {

  private final ReentrantLock lock = new ReentrantLock();

  public SynchronizedCache(Cache delegate) {
    super(delegate);
  }

  @Override
  public int getSize() {
    lock.lock();
    try {
      return super.getSize();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    lock.lock();
    try {
      super.putObject(key, object);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object getObject(Object key) {
    lock.lock();
    try {
      return super.getObject(key);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object removeObject(Object key) {
    lock.lock();
    try {
      return super.removeObject(key);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      super.clear();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int hashCode() {
    return getDelegate().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getDelegate().equals(obj);
  }

}
