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