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

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.UUID;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * Tests for {@link LazyReference}.
 *
 * @author Kazuki Shimizu
 * @since 3.5.0
 */
public class LazyReferenceTest {

  @Test
  public void shouldReturnSameInstance() {
    LazyReference<UUID> uuid = LazyReference.of(UUID::randomUUID);
    Assertions.assertThat(uuid.get()).isSameAs(uuid.get());
    Assertions.assertThat(uuid.get()).isSameAs(uuid.getNullable());
    Assertions.assertThat(uuid.getNullable()).isSameAs(uuid.getNullable());
  }

  @Test
  public void shouldAllowToReturnNullOnGetNullable() {
    LazyReference<UUID> uuid = LazyReference.of(() -> null);
    Assertions.assertThat(uuid.getNullable()).isNull();
  }

  @Test
  public void shouldNotAllowToReturnNullOnGet() {
    LazyReference<UUID> uuid = LazyReference.of(() -> null);
    when(uuid).get();
    then(caughtException())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Expected lazy evaluation to yield a non-null object but got null.");
  }

  @Test
  public void shouldNotAllowNullSupplier() {
    try {
      LazyReference.of(null);
    } catch (IllegalArgumentException e) {
      Assertions.assertThat(e).hasMessage("'supplier' must be specified.");
    }
  }

}
