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
