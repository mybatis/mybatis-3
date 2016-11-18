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

import java.util.Deque;
import java.util.LinkedList;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheDecorator;

/**
 * FIFO (first in, first out) cache decorator.
 *
 * @author Clinton Begin
 */
public class FifoCache extends CacheDecorator {

  private final Deque<Object> keyList;
  private int size;

  public FifoCache(Cache delegate) {
    super(delegate);
    this.keyList = new LinkedList<>();
    this.size = 1024;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    cycleKeyList(key);
    super.putObject(key, value);
  }

  @Override
  public Object getObject(Object key) {
    return super.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    keyList.remove(key);
    return super.removeObject(key);
  }

  @Override
  public void clear() {
    super.clear();
    keyList.clear();
  }

  private void cycleKeyList(Object key) {
    keyList.addLast(key);
    if (keyList.size() > size) {
      Object oldestKey = keyList.removeFirst();
      super.removeObject(oldestKey);
    }
  }

}
