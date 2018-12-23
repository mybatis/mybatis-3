package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.Test;

public class BlockingCacheTest {
  private static final int THREAD_NUM = 5;
  private static final String CACHE_KEY = "cache_key";

  @Test
  public void testBlockingCacheByMultiThread() throws InterruptedException {
    Cache cache = new BlockingCache(new PerpetualCache("test_blocking_cache"));
    for (int i = 0; i < THREAD_NUM; i++) {
      Thread thread = new Thread(() -> cache.getObject(CACHE_KEY));
      thread.start();
      thread.join();
    }
  }
}
