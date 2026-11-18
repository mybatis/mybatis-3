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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.SynchronizedCache;
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

  /** A read/write blocking cache must release the key if deserialization fails. */
  @Test
  void blockingReadWriteCacheReleasesKeyLockWhenDelegateReadFails() throws Exception {
    PerpetualCache perpetual = new PerpetualCache("blockingReadWrite");
    BlockingCache cache = new BlockingCache(new SynchronizedCache(new SerializedCache(perpetual)));
    // bounded wait; the production default (0) waits forever and turns the leaked lock into a hang
    cache.setTimeout(100);

    // not a serialized stream, so the delegate read fails while the key lock is held
    perpetual.putObject(0, new byte[] { 1, 2, 3 });
    CacheException readFailure = assertThrows(CacheException.class, () -> cache.getObject(0));
    assertTrue(readFailure.getMessage().startsWith("Error deserializing object."),
        "the delegate read failure must stay the primary exception: " + readFailure.getMessage());
    assertTrue(readFailure.getCause() instanceof IOException,
        "the original IOException must not be replaced: " + readFailure.getCause());

    // repair the entry behind the decorators; releasing the lock does not repair the payload
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(new CachingObject(1));
    }
    perpetual.putObject(0, bos.toByteArray());

    // the next read must reach the delegate again and return the repaired value
    assertEquals(new CachingObject(1), cache.getObject(0));
  }

  @Test
  void blockingCacheKeepsKeyLockForOrdinaryMissUntilPut() {
    PerpetualCache perpetual = new PerpetualCache("blockingMiss");
    BlockingCache cache = new BlockingCache(new SynchronizedCache(new SerializedCache(perpetual)));
    cache.setTimeout(100);

    // an ordinary miss keeps the key locked for the next reader, as documented
    assertNull(cache.getObject(0));
    CacheException blocked = assertThrows(CacheException.class, () -> cache.getObject(0));
    assertTrue(blocked.getMessage().contains("Couldn't get a lock in 100"), blocked.getMessage());

    // filling the entry releases the lock for subsequent readers
    cache.putObject(0, new CachingObject(2));
    assertEquals(new CachingObject(2), cache.getObject(0));
  }

  @Test
  void blockingCacheReleasesKeyLockWhenDelegateReadThrowsError() {
    Error delegateFailure = new OutOfMemoryError("deliberate delegate read failure");
    PerpetualCache failingOnce = new PerpetualCache("blockingError") {
      private boolean failed;

      @Override
      public Object getObject(Object key) {
        if (!failed) {
          failed = true;
          throw delegateFailure;
        }
        return new CachingObject(3);
      }
    };
    BlockingCache cache = new BlockingCache(failingOnce);
    // bounded wait; a lock kept by the failed read would surface as a CacheException instead of hanging the test
    cache.setTimeout(100);

    assertSame(delegateFailure, assertThrows(Error.class, () -> cache.getObject(0)));

    assertEquals(new CachingObject(3), cache.getObject(0));
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
