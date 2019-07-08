/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.scripting.xmltags;

import java.util.Map;
import java.util.UUID;

import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OgnlClassResolverTest {

  private TypeAliasRegistry registry = new TypeAliasRegistry();
  private OgnlClassResolver classResolver = new OgnlClassResolver(registry);

  @Test
  void shouldResolveBuildInClassThatManagedOnTypeAliasRegistry() {
    for (Map.Entry<String, Class<?>> entry : registry.getTypeAliases().entrySet()) {
      Assertions.assertThat(classResolver.classForName(entry.getKey(), null)).isSameAs(entry.getValue());
      if (entry.getValue().isPrimitive() || entry.getValue().isArray() && entry.getValue().getComponentType().isPrimitive()) {
        Assertions.assertThat(classResolver.classForName("_" + entry.getValue().getSimpleName(), null)).isSameAs(entry.getValue());
      } else {
        Assertions.assertThat(classResolver.classForName(entry.getValue().getSimpleName(), null)).isSameAs(entry.getValue());
      }
    }
  }

  @Test
  void shouldResolveJavaLangClass() {
    Assertions.assertThat(classResolver.classForName("Math", null)).isSameAs(Math.class);
  }

  @Test
  void shouldResolveClassByFQCN() {
    Assertions.assertThat(classResolver.classForName("java.util.UUID", null)).isSameAs(UUID.class);
  }

  @Test
  void shouldResolveClassByAlias() {
    registry.registerAlias(UUID.class);
    Assertions.assertThat(classResolver.classForName("uuid", null)).isSameAs(UUID.class);
  }

  @Test
  void shouldNotResolveNoJavaLangClassWhenDoesNotManagedOnTypeAliasRegistry() {
    Assertions.assertThatThrownBy(() -> classResolver.classForName("UUID", null))
      .isInstanceOf(TypeException.class)
      .hasCauseInstanceOf(ClassNotFoundException.class);
  }

  @Test
  void shouldNotResolveWhenSpecifyDoesNotExists() {
    Assertions.assertThatThrownBy(() -> classResolver.classForName("com.example.LikeUtils", null))
      .isInstanceOf(TypeException.class)
      .hasCauseInstanceOf(ClassNotFoundException.class);
  }

}
