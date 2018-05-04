/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.util;

import java.util.function.Supplier;

/**
 * A reference to delay the creation of an object using a {@link Supplier}.
 * <p>
 * Note, that no concurrency control is applied during the lookup of {@link #get()} or {@link #getNullable()},
 * which means in concurrent access scenarios, the provided {@link Supplier} can be called multiple times.
 *
 * @param <T> object type
 * @author Kazuki Shimizu
 * @since 3.5.0
 */
public class LazyReference<T> {

  private final Supplier<? extends T> supplier;
  private T object = null;
  private boolean resolved = false;

  private LazyReference(Supplier<? extends T> supplier) {
    this.supplier = supplier;
  }

  /**
   * Creates a new {@link LazyReference} to produce an object lazily.
   *
   * @param <T> the type of which to produce an object of eventually.
   * @param supplier the {@link Supplier} to create the object lazily.
   * @return a new {@link LazyReference}
   * @throws IllegalArgumentException If specified supplier is {@literal null}
   */
  public static <T> LazyReference<T> of(Supplier<? extends T> supplier) {
    if (supplier == null) {
      throw new IllegalArgumentException("'supplier' must be specified.");
    }
    return new LazyReference<>(supplier);
  }

  /**
   * Returns the object created by the specified {@link Supplier}.
   * lookups.
   *
   * @return the object created by the specified {@link Supplier}
   * @throws IllegalStateException If the object created by the configured {@link Supplier} is {@literal null}
   */
  public T get() {
    T object = getNullable();
    if (object == null) {
      throw new IllegalStateException("Expected lazy evaluation to yield a non-null object but got null.");
    }
    return object;
  }

  /**
   * Returns the object created by the specified {@link Supplier}.
   *
   * @return the object created by the specified {@link Supplier}
   */
  public T getNullable() {
    T resolvedObject = this.object;
    if (this.resolved) {
      return resolvedObject;
    }
    T creationObject = supplier.get();
    this.object = creationObject;
    this.resolved = true;
    return creationObject;
  }

}
