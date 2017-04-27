/**
 *    Copyright 2009-2015 the original author or authors.
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

import org.apache.ibatis.cache.decorators.LoggingCache;
import org.apache.ibatis.cache.decorators.ScheduledCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import static org.junit.Assert.*;
import org.junit.Test;

public class ScheduledCacheTest {

  @Test
  public void shouldDemonstrateHowAllObjectsAreFlushedAfterBasedOnTime() throws Exception {
    Cache cache = new PerpetualCache("DefaultCache");
    cache = new ScheduledCache(cache);
    ((ScheduledCache) cache).setClearInterval(2500);
    cache = new LoggingCache(cache);
    for (int i = 0; i < 100; i++) {
      cache.putObject(i, i);
      assertEquals(i, cache.getObject(i));
    }
    Thread.sleep(5000);
    assertEquals(0, cache.getSize());
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new PerpetualCache("DefaultCache");
    cache = new ScheduledCache(cache);
    ((ScheduledCache) cache).setClearInterval(60000);
    cache = new LoggingCache(cache);
    cache.putObject(0, 0);
    assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new PerpetualCache("DefaultCache");
    cache = new ScheduledCache(cache);
    ((ScheduledCache) cache).setClearInterval(60000);
    cache = new LoggingCache(cache);
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    assertNotNull(cache.getObject(0));
    assertNotNull(cache.getObject(4));
    cache.clear();
    assertNull(cache.getObject(0));
    assertNull(cache.getObject(4));
  }

}