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

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.StandaloneResultMap;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class StandaloneResultMapTest {
  @Test
  void standaloneResultMapWithNoMappingsShouldFail() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, MapperWithNoMappings.class);
    assertThatExceptionOfType(BuilderException.class).isThrownBy(builder::parse)
        .withMessage("If there is no type discriminator, then StandaloneResultMap annotation "
            + "requires at least one constructor argument or property mapping");
  }

  @Test
  void standaloneResultMapWithPrivateFieldShouldFail() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, MapperWithPrivateStaticField.class);
    assertThatExceptionOfType(BuilderException.class).isThrownBy(builder::parse)
        .withMessage("StandaloneResultMap annotation can only be used on accessible fields");
  }

  @Test
  void standaloneResultMapWithNonStaticFieldShouldFail() {
    Configuration configuration = new Configuration();
    MapperAnnotationBuilder builder = new MapperAnnotationBuilder(configuration, MapperWithNonStaticField.class);
    assertThatExceptionOfType(BuilderException.class).isThrownBy(builder::parse)
        .withMessage("StandaloneResultMap annotation can only be used on static fields");
  }

  private interface MapperWithNoMappings {
    @StandaloneResultMap(javaType = String.class)
    String badMap = "badMap";
  }

  private static class MapperWithPrivateStaticField {
    @StandaloneResultMap(javaType = String.class, constructorArguments = { @Arg(column = "id", javaType = int.class) })
    private static String badMap = "badMap";
  }

  private static class MapperWithNonStaticField {
    @StandaloneResultMap(javaType = String.class, constructorArguments = { @Arg(column = "id", javaType = int.class) })
    public String badMap = "badMap";
  }
}
