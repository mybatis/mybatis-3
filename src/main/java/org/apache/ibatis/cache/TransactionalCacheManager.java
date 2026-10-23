/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.cache.decorators.TransactionalCache;

/**
 * @author Clinton Begin
 */
public class TransactionalCacheManager {

  private final Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

  public void clear(Cache cache) {
    getTransactionalCache(cache).clear();
  }

  public Object getObject(Cache cache, CacheKey key) {
    return getTransactionalCache(cache).getObject(key);
  }

  public void putObject(Cache cache, CacheKey key, Object value) {
    getTransactionalCache(cache).putObject(key, value);
  }

  public void commit() {
    try {
      for (TransactionalCache txCache : transactionalCaches.values()) {
        txCache.commit();
      }
    } catch (RuntimeException | Error e) {
      // the failing cache already released its own locks, this cleans up the namespaces that were not committed yet
      rollbackAfterFailure(e);
      throw e;
    }
  }

  public void rollback() {
    for (TransactionalCache txCache : transactionalCaches.values()) {
      txCache.rollback();
    }
  }

  private void rollbackAfterFailure(Throwable firstFailure) {
    for (TransactionalCache txCache : transactionalCaches.values()) {
      try {
        txCache.rollback();
      } catch (RuntimeException | Error cleanupFailure) {
        // keep the original failure, roll back the remaining namespaces anyway, and never suppress a failure on itself
        if (cleanupFailure != firstFailure) {
          firstFailure.addSuppressed(cleanupFailure);
        }
      }
    }
  }

  private TransactionalCache getTransactionalCache(Cache cache) {
    return transactionalCaches.computeIfAbsent(cache, TransactionalCache::new);
  }

}
