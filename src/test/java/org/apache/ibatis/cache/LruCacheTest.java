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