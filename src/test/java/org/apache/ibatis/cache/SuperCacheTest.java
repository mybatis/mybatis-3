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
package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SuperCacheTest {

  @Test
  public void shouldDemonstrate5LevelSuperCacheHandlesLotsOfEntriesWithoutCrashing() {
    final int N = 100000;
    Cache cache = new PerpetualCache("default");
    cache = new LruCache(cache);
    cache = new FifoCache(cache);
    cache = new SoftCache(cache);
    cache = new WeakCache(cache);
    cache = new ScheduledCache(cache);
    cache = new SerializedCache(cache);
//    cache = new LoggingCache(cache);
    cache = new SynchronizedCache(cache);
    cache = new TransactionalCache(cache);
    for (int i = 0; i < N; i++) {
      cache.putObject(i, i);
      ((TransactionalCache) cache).commit();
      Object o = cache.getObject(i);
      assertTrue(o == null || i == ((Integer) o));
    }
    assertTrue(cache.getSize() < N);
  }


}
