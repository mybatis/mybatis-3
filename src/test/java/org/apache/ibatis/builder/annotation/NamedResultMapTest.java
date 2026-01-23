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
package org.apache.ibatis.builder.annotation;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.apache.ibatis.annotations.NamedResultMap;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class NamedResultMapTest {
  @Test
  void namedResultMapWithNoMappingsShouldFail() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, MapperWithNoMappings.class);
    assertThatExceptionOfType(BuilderException.class).isThrownBy(builder::parse)
        .withMessage("If there is no type discriminator, then the NamedResultMap annotation "
            + "requires at least one constructor argument or property mapping");
  }

  @NamedResultMap(id = "badMap", javaType = String.class)
  private interface MapperWithNoMappings {
  }
}
