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
package org.apache.ibatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;

/**
 * The annotation that specify to use cache on namespace(e.g. mapper interface).
 *
 * <p>
 * <b>How to use:</b>
 *
 * <pre>
 * &#064;CacheNamespace(implementation = CustomCache.class, properties = {
 *   &#064;Property(name = "host", value = "${mybatis.cache.host}"),
 *   &#064;Property(name = "port", value = "${mybatis.cache.port}"),
 *   &#064;Property(name = "name", value = "usersCache")
 * })
 * public interface UserMapper {
 *   // ...
 * }
 * </pre>
 *
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespace {

  /**
   * Returns the cache implementation type to use.
   *
   * @return the cache implementation type
   */
  Class<? extends Cache> implementation() default PerpetualCache.class;

  /**
   * Returns the cache evicting implementation type to use.
   *
   * @return the cache evicting implementation type
   */
  Class<? extends Cache> eviction() default LruCache.class;

  /**
   * Returns the flush interval.
   *
   * @return the flush interval
   */
  long flushInterval() default 0;

  /**
   * Return the cache size.
   *
   * @return the cache size
   */
  int size() default 1024;

  /**
   * Returns whether use read/write cache.
   *
   * @return {@code true} if use read/write cache; {@code false} if otherwise
   */
  boolean readWrite() default true;

  /**
   * Returns whether block the cache at request time or not.
   *
   * @return {@code true} if block the cache; {@code false} if otherwise
   */
  boolean blocking() default false;

  /**
   * Returns property values for a implementation object.
   *
   * @return property values
   * @since 3.4.2
   */
  Property[] properties() default {};

}
