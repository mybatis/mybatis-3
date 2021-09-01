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
package org.apache.ibatis.mapping;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;

import org.apache.ibatis.builder.InitializingObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CacheBuilderTest {

  @Test
  void testInitializing() {
    InitializingCache cache = unwrap(new CacheBuilder("test").implementation(InitializingCache.class).build());

    Assertions.assertThat(cache.initialized).isTrue();
  }

  @Test
  void testInitializingFailure() {
    when(() -> new CacheBuilder("test").implementation(InitializingFailureCache.class).build());
    then(caughtException()).isInstanceOf(CacheException.class)
      .hasMessage("Failed cache initialization for 'test' on 'org.apache.ibatis.mapping.CacheBuilderTest$InitializingFailureCache'");
  }

  @SuppressWarnings("unchecked")
  private <T> T unwrap(Cache cache) {
    Field field;
    try {
      field = cache.getClass().getDeclaredField("delegate");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
    try {
      field.setAccessible(true);
      return (T) field.get(cache);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } finally {
      field.setAccessible(false);
    }
  }

  private static class InitializingCache extends PerpetualCache implements InitializingObject {

    private boolean initialized;

    public InitializingCache(String id) {
      super(id);
    }

    @Override
    public void initialize() {
      this.initialized = true;
    }

  }

  private static class InitializingFailureCache extends PerpetualCache implements InitializingObject {

    public InitializingFailureCache(String id) {
      super(id);
    }

    @Override
    public void initialize() {
      throw new IllegalStateException("error");
    }

  }

}
