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
package org.apache.ibatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session. Entries are sent to
 * the cache when commit is called or discarded if the Session is rolled back. Blocking cache support has been added.
 * Therefore any get() that returns a cache miss will be followed by a put() so any lock associated with the key can be
 * released.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class TransactionalCache implements Cache {

  private static final Log log = LogFactory.getLog(TransactionalCache.class);

  private final Cache delegate;
  private boolean clearOnCommit;
  private final Map<Object, Object> entriesToAddOnCommit;
  private final Set<Object> entriesMissedInCache;

  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  @Override
  public Object getObject(Object key) {
    // issue #116
    Object object = delegate.getObject(key);
    if (object == null) {
      entriesMissedInCache.add(key);
    }
    // issue #146
    if (clearOnCommit) {
      return null;
    }
    return object;
  }

  @Override
  public void putObject(Object key, Object object) {
    entriesToAddOnCommit.put(key, object);
  }

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  @Override
  public void clear() {
    clearOnCommit = true;
    entriesToAddOnCommit.clear();
  }

  public void commit() {
    try {
      if (clearOnCommit) {
        delegate.clear();
      }
      flushPendingEntries();
    } catch (RuntimeException | Error e) {
      // the flush is abandoned: release the locks still owned here, reset() then discards the pending entries
      unlockUnflushedMissedEntries(e);
      throw e;
    } finally {
      reset();
    }
  }

  public void rollback() {
    unlockMissedEntries();
    reset();
  }

  private void reset() {
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  private void flushPendingEntries() {
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      // the delegate releases the lock of a key even when the put fails, so it is not ours to release anymore
      entriesMissedInCache.remove(entry.getKey());
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    // only entries without a buffered value are left: they stay locked until they are handed to the delegate
    for (Iterator<Object> iterator = entriesMissedInCache.iterator(); iterator.hasNext();) {
      Object entry = iterator.next();
      iterator.remove();
      delegate.putObject(entry, null);
    }
  }

  /** Releases the locks of the missed entries that were not handed to the delegate yet, i.e. still owned here. */
  private void unlockUnflushedMissedEntries(Throwable originalFailure) {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (RuntimeException | Error cleanupFailure) {
        log.warn("Unexpected exception while notifying a failed commit to the cache adapter. "
            + "Consider upgrading your cache adapter to the latest version. Cause: " + cleanupFailure);
        // a failing cleanup must neither hide the original failure nor stop the release of the remaining locks
        if (cleanupFailure != originalFailure) {
          originalFailure.addSuppressed(cleanupFailure);
        }
      }
    }
  }

  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
            + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
      }
    }
  }

}
