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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.TransactionalCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.jupiter.api.Test;

class SerializedCacheTest {

  @Test
  void shouldDemonstrateSerializedObjectAreEqual() {
    SerializedCache cache = new SerializedCache(new PerpetualCache("default"));
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, new CachingObject(i));
    }
    for (int i = 0; i < 5; i++) {
      assertEquals(new CachingObject(i), cache.getObject(i));
    }
  }

  @Test
  void shouldDemonstrateNullsAreSerializable() {
    SerializedCache cache = new SerializedCache(new PerpetualCache("default"));
    for (int i = 0; i < 5; i++) {
      cache.putObject(i, null);
    }
    for (int i = 0; i < 5; i++) {
      assertNull(cache.getObject(i));
    }
  }

  @Test
  void throwExceptionWhenTryingToCacheNonSerializableObject() {
    SerializedCache cache = new SerializedCache(new PerpetualCache("default"));
    assertThrows(CacheException.class, () -> cache.putObject(0, new CachingObjectWithoutSerializable(0)));
  }

  /**
   * A failed flush must release the locks it still holds and must be harmless when the cache is committed on close.
   */
  @Test
  void failedCommitMustReleaseTheLocksItStillHoldsAndSurviveTheCommitOnClose() {
    BlockingCache blockingCache = new BlockingCache(new SerializedCache(new PerpetualCache("default")));
    blockingCache.setTimeout(100);
    TransactionalCache transactionalCache = new TransactionalCache(blockingCache);
    // as after an update: the delegate is cleared before the buffered entries are flushed
    transactionalCache.clear();
    Object firstKey = "first";
    Object secondKey = "second";
    transactionalCache.getObject(firstKey);
    transactionalCache.putObject(firstKey, new CachingObjectWithoutSerializable(1));
    transactionalCache.getObject(secondKey);
    transactionalCache.putObject(secondKey, new CachingObjectWithoutSerializable(2));

    assertThrows(CacheException.class, transactionalCache::commit);
    // the failure is not replaced by an IllegalStateException when the session is closed
    assertDoesNotThrow(transactionalCache::commit);
    // both locks were released: a leaked lock fails here after the blocking cache timeout
    assertNull(blockingCache.getObject(firstKey));
    assertNull(blockingCache.getObject(secondKey));
  }

  /** A failing cleanup must not replace the original failure and must not abort the release of the remaining locks. */
  @Test
  void failedCommitMustKeepTheOriginalFailureWhenReleasingTheRemainingLocksFails() {
    Cache delegate = mock(Cache.class);
    CacheException writeFailure = new CacheException("write failed");
    AssertionError releaseFailure = new AssertionError("cannot release");
    doThrow(writeFailure).when(delegate).putObject("value", "v");
    doThrow(releaseFailure).when(delegate).removeObject("releaseFails");
    TransactionalCache transactionalCache = new TransactionalCache(delegate);
    transactionalCache.putObject("value", "v");
    // two entries that were locked but never handed to the delegate, released in no guaranteed order
    transactionalCache.getObject("releaseFails");
    transactionalCache.getObject("released");

    CacheException thrown = assertThrows(CacheException.class, transactionalCache::commit);

    assertSame(writeFailure, thrown);
    assertEquals(1, thrown.getSuppressed().length);
    assertSame(releaseFailure, thrown.getSuppressed()[0]);
    // the second commit (on close) must not release a lock again, i.e. no further cleanup is attempted
    assertDoesNotThrow(transactionalCache::commit);
    verify(delegate, times(1)).removeObject("releaseFails");
    verify(delegate, times(1)).removeObject("released");
    verify(delegate, times(2)).removeObject(any());
  }

  /** A failing clear must still release the locks of the missed entries and leave nothing for a later commit. */
  @Test
  void failedClearMustStillReleaseTheLocksOfTheMissedEntries() {
    Cache delegate = mock(Cache.class);
    CacheException clearFailure = new CacheException("clear failed");
    doThrow(clearFailure).when(delegate).clear();
    TransactionalCache transactionalCache = new TransactionalCache(delegate);
    transactionalCache.clear();
    transactionalCache.getObject("first");
    transactionalCache.getObject("second");

    assertSame(clearFailure, assertThrows(CacheException.class, transactionalCache::commit));

    assertDoesNotThrow(transactionalCache::commit);
    verify(delegate, times(1)).clear();
    verify(delegate, times(1)).removeObject("first");
    verify(delegate, times(1)).removeObject("second");
    verify(delegate, times(2)).removeObject(any());
  }

  /** A cleanup failing with the original failure must neither self suppress it nor stop the other namespaces. */
  @Test
  void failedCommitMustNotSelfSuppressOrStopTheCleanupOfTheOtherNamespaces() {
    AssertionError adapterFailure = new AssertionError("adapter failure");
    AtomicInteger cleanupAttempts = new AtomicInteger();
    TransactionalCacheManager manager = new TransactionalCacheManager();
    for (int i = 0; i < 3; i++) {
      Cache adapter = mock(Cache.class);
      doThrow(adapterFailure).when(adapter).clear();
      doAnswer(invocation -> {
        cleanupAttempts.incrementAndGet();
        throw adapterFailure;
      }).when(adapter).removeObject(any());
      manager.clear(adapter);
      manager.getObject(adapter, new CacheKey(new Object[] { i }));
    }

    assertSame(adapterFailure, assertThrows(AssertionError.class, manager::commit));

    // every namespace is cleaned up exactly once, whichever namespace failed first
    assertEquals(3, cleanupAttempts.get());
    assertEquals(0, adapterFailure.getSuppressed().length);
  }

  static class CachingObject implements Serializable {
    private static final long serialVersionUID = 1L;
    int x;

    public CachingObject(int x) {
      this.x = x;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CachingObject obj = (CachingObject) o;
      return x == obj.x;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x);
    }
  }

  static class CachingObjectWithoutSerializable {
    int x;

    public CachingObjectWithoutSerializable(int x) {
      this.x = x;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CachingObjectWithoutSerializable obj = (CachingObjectWithoutSerializable) o;
      return x == obj.x;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x);
    }
  }
}
