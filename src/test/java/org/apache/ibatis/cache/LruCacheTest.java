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

import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import static org.junit.Assert.*;
import org.junit.Test;

public class LruCacheTest {

  @Test
  public void shouldRemoveLeastRecentlyUsedItemInBeyondFiveEntries() {
    LruCache cache = new LruCache(new PerpetualCache("default"));
    cache.setSize(5);
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, i);
    }
    assertEquals(0, cache.getObject(0));
    cache.putObject(5, 5);
    assertNull(cache.getObject(1));
    assertEquals(5, cache.getSize());
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new LruCache(new PerpetualCache("default"));
    cache.putObject(0, 0);
    assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new LruCache(new PerpetualCache("default"));
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