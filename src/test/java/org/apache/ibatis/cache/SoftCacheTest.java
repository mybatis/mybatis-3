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

import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import static org.junit.Assert.*;
import org.junit.Test;

public class SoftCacheTest {

  @Test
  public void shouldDemonstrateObjectsBeingCollectedAsNeeded() throws Exception {
    final int N = 3000000;
    SoftCache cache = new SoftCache(new PerpetualCache("default"));
    for (int i = 0; i < N; i++) {
      byte[] array = new byte[5001]; //waste a bunch of memory
      array[5000] = 1;
      cache.putObject(i, array);
      Object value = cache.getObject(i);
      if (cache.getSize() < i + 1) {
        //System.out.println("Cache exceeded with " + (i + 1) + " entries.");
        break;
      }
    }
    assertTrue(cache.getSize() < N);
  }


  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new SoftCache(new PerpetualCache("default"));
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Object value = cache.getObject(i);
      assertTrue(value == null || value.equals(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    Cache cache = new SoftCache(new PerpetualCache("default"));
    cache.putObject(0, 0);
    assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    Cache cache = new SoftCache(new PerpetualCache("default"));
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