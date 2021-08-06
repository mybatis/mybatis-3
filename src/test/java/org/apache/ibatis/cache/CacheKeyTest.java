/*
 *    Copyright 2009-2021 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.junit.jupiter.api.Test;

class CacheKeyTest {

  @Test
  void shouldTestCacheKeysEqual() {
    Date date = new Date();
    CacheKey key1 = new CacheKey(new Object[] { 1, "hello", null, new Date(date.getTime()) });
    CacheKey key2 = new CacheKey(new Object[] { 1, "hello", null, new Date(date.getTime()) });
    assertEquals(key1, key2);
    assertEquals(key2, key1);
    assertEquals(key1.hashCode(), key2.hashCode());
    assertEquals(key1.toString(), key2.toString());
  }

  @Test
  void shouldTestCacheKeysNotEqualDueToDateDifference() throws Exception {
    CacheKey key1 = new CacheKey(new Object[] { 1, "hello", null, new Date() });
    Thread.sleep(1000);
    CacheKey key2 = new CacheKey(new Object[] { 1, "hello", null, new Date() });
    assertNotEquals(key1, key2);
    assertNotEquals(key2, key1);
    assertNotEquals(key1.hashCode(), key2.hashCode());
    assertNotEquals(key1.toString(), key2.toString());
  }

  @Test
  void shouldTestCacheKeysNotEqualDueToOrder() throws Exception {
    CacheKey key1 = new CacheKey(new Object[] { 1, "hello", null });
    Thread.sleep(1000);
    CacheKey key2 = new CacheKey(new Object[] { 1, null, "hello" });
    assertNotEquals(key1, key2);
    assertNotEquals(key2, key1);
    assertNotEquals(key1.hashCode(), key2.hashCode());
    assertNotEquals(key1.toString(), key2.toString());
  }

  @Test
  void shouldDemonstrateEmptyAndNullKeysAreEqual() {
    CacheKey key1 = new CacheKey();
    CacheKey key2 = new CacheKey();
    assertEquals(key1, key2);
    assertEquals(key2, key1);
    key1.update(null);
    key2.update(null);
    assertEquals(key1, key2);
    assertEquals(key2, key1);
    key1.update(null);
    key2.update(null);
    assertEquals(key1, key2);
    assertEquals(key2, key1);
  }

  @Test
  void shouldTestCacheKeysWithBinaryArrays() {
    byte[] array1 = new byte[] { 1 };
    byte[] array2 = new byte[] { 1 };
    CacheKey key1 = new CacheKey(new Object[] { array1 });
    CacheKey key2 = new CacheKey(new Object[] { array2 });
    assertEquals(key1, key2);
  }

  @Test
  void throwExceptionWhenTryingToUpdateNullCacheKey() {
    CacheKey cacheKey = CacheKey.NULL_CACHE_KEY;
    assertThrows(CacheException.class, () -> cacheKey.update("null"));
  }

  @Test
  void throwExceptionWhenTryingToUpdateAllNullCacheKey() {
    CacheKey cacheKey = CacheKey.NULL_CACHE_KEY;
    assertThrows(CacheException.class, () -> cacheKey.updateAll(new Object[]{"null", "null"}));
  }

  @Test
  void shouldDemonstrateClonedNullCacheKeysAreEqual() throws Exception {
    CacheKey cacheKey = CacheKey.NULL_CACHE_KEY;
    CacheKey clonedCacheKey = cacheKey.clone();
    assertEquals(cacheKey, clonedCacheKey);
    assertEquals(cacheKey.hashCode(), clonedCacheKey.hashCode());
  }

  @Test
  void serializationExceptionTest() {
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(new Object());
    assertThrows(NotSerializableException.class, () -> {
      serialize(cacheKey);
    });
  }

  @Test
  void serializationTest() throws Exception {
    CacheKey cacheKey = new CacheKey();
    cacheKey.update("serializable");
    assertEquals(cacheKey, serialize(cacheKey));
  }

  private static <T> T serialize(T object) throws Exception {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      new ObjectOutputStream(baos).writeObject(object);

      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return (T) new ObjectInputStream(bais).readObject();
  }

}
