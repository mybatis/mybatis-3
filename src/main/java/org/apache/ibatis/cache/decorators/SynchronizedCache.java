/*
 *    Copyright 2009-2022 the original author or authors.
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

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.impl.DelegateCache;

/**
 * @author Clinton Begin
 */
public class SynchronizedCache extends DelegateCache {

  public SynchronizedCache(Cache delegate) {
    super(delegate);
  }

  @Override
  public synchronized int getSize() {
    return super.getSize();
  }

  @Override
  public synchronized void putObject(Object key, Object object) {
    super.putObject(key, object);
  }

  @Override
  public synchronized Object getObject(Object key) {
    return super.getObject(key);
  }

  @Override
  public synchronized Object removeObject(Object key) {
    return super.removeObject(key);
  }

  @Override
  public synchronized void clear() {
    super.clear();
  }

}
