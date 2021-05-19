/*
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
      assertEquals(null, cache.getObject(i));
    }
  }

  @Test
  void throwExceptionWhenTryingToCacheNonSerializableObject() {
    SerializedCache cache = new SerializedCache(new PerpetualCache("default"));
    assertThrows(CacheException.class,
      () -> cache.putObject(0, new CachingObjectWithoutSerializable(0)));
  }

  static class CachingObject implements Serializable {
    int x;

    public CachingObject(int x) {
      this.x = x;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
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
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CachingObjectWithoutSerializable obj = (CachingObjectWithoutSerializable) o;
      return x == obj.x;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x);
    }
  }
}
