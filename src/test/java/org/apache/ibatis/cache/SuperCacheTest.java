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
