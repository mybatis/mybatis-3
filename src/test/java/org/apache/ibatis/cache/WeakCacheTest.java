package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

public class WeakCacheTest {

  @Test
  public void shouldDemonstrateObjectsBeingCollectedAsNeeded() {
    final int N = 3000000;
    WeakCache cache = new WeakCache(new PerpetualCache("default"));
    for (int i = 0; i < N; i++) {
      cache.putObject(i, i);
      if (cache.getSize() < i + 1) {
        //System.out.println("Cache exceeded with " + (i + 1) + " entries.");
        break;
      }
    }
    assertTrue(cache.getSize() < N);
  }


  @Test
  public void shouldDemonstrateCopiesAreEqual() {
    Cache cache = new WeakCache(new PerpetualCache("default"));
    cache = new SerializedCache(cache);
    for (int i = 0; i < 1000; i++) {
      cache.putObject(i, i);
      Object value = cache.getObject(i);
      assertTrue(value == null || value.equals(i));
    }
  }

  @Test
  public void shouldRemoveItemOnDemand() {
    WeakCache cache = new WeakCache(new PerpetualCache("default"));
    cache.putObject(0, 0);
    assertNotNull(cache.getObject(0));
    cache.removeObject(0);
    assertNull(cache.getObject(0));
  }

  @Test
  public void shouldFlushAllItemsOnDemand() {
    WeakCache cache = new WeakCache(new PerpetualCache("default"));
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
