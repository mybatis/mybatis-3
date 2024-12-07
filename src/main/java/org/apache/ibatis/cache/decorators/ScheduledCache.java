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

import java.util.concurrent.TimeUnit;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheDecorator;

/**
 * @author Clinton Begin
 */
public class ScheduledCache extends CacheDecorator {

  protected long clearInterval;
  protected long lastClear;

  public ScheduledCache(Cache delegate) {
    super(delegate);
    this.clearInterval = TimeUnit.HOURS.toMillis(1);
    this.lastClear = System.currentTimeMillis();
  }

  public void setClearInterval(long clearInterval) {
    this.clearInterval = clearInterval;
  }

  @Override
  public int getSize() {
    clearWhenStale();
    return super.getSize();
  }

  @Override
  public void putObject(Object key, Object object) {
    clearWhenStale();
    super.putObject(key, object);
  }

  @Override
  public Object getObject(Object key) {
    return clearWhenStale() ? null : super.getObject(key);
  }

  @Override
  public Object removeObject(Object key) {
    clearWhenStale();
    return super.removeObject(key);
  }

  @Override
  public void clear() {
    lastClear = System.currentTimeMillis();
    super.clear();
  }

  @Override
  public int hashCode() {
    return getDelegate().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getDelegate().equals(obj);
  }

  private boolean clearWhenStale() {
    if (System.currentTimeMillis() - lastClear > clearInterval) {
      clear();
      return true;
    }
    return false;
  }

}
