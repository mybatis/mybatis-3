/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author wuwen
 */
public class CacheDecoratorTest {

  @Test
  public void getSpecifiedDecorator() {
    Cache cache = new PerpetualCache("default");
    cache = new LruCache(cache);
    cache = new LoggingCache(cache);
    cache = new SynchronizedCache(cache);

    cache.putObject("hello", System.currentTimeMillis());

    cache.getObject("hello");

    LoggingCache loggingCache = findCacheDecorator((CacheDecorator)cache, LoggingCache.class);

    assertNotNull(loggingCache);
    assertEquals(1,loggingCache.getHits());

  }

  private <T> T findCacheDecorator(CacheDecorator cache, Class<T> type) {
    Cache delegate = cache.getDelegate();

    if (delegate.getClass().equals(type)) {
      return (T) delegate;
    } else if (delegate instanceof  CacheDecorator) {
      return findCacheDecorator((CacheDecorator)delegate, type);
    }

    return null;
  }

}
