package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.TransactionalCache;

import java.util.HashMap;
import java.util.Map;

public class TransactionalCacheManager {

  private Map<Cache, TransactionalCache> transactionalCaches = new HashMap<Cache, TransactionalCache>();

  public void clear(Cache cache) {
    getTransactionalCache(cache).clear();
  }

  public void putObject(Cache cache, CacheKey key, Object value) {
    getTransactionalCache(cache).putObject(key, value);
  }

  public void commit() {
    for (TransactionalCache txCache : transactionalCaches.values()) {
      txCache.commit();
    }
  }

  public void rollback() {
    for (TransactionalCache txCache : transactionalCaches.values()) {
      txCache.rollback();
    }
  }

  private TransactionalCache getTransactionalCache(Cache cache) {
    TransactionalCache txCache = transactionalCaches.get(cache);
    if (txCache == null) {
      txCache = new TransactionalCache(cache);
      transactionalCaches.put(cache, txCache);
    }
    return txCache;
  }

}
