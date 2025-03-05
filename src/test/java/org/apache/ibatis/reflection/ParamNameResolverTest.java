/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class ParamNameResolverTest {

  // @formatter:off
  class A {
    void m1(@Param("p") Integer p) {}
    void m2(List<String> p) {}
    void m3(Integer[] p) {}
  }
  // @formatter:on

  @Test
  void testGetTypeSimple() throws Exception {
    Class<?> clazz = A.class;
    Method method = clazz.getDeclaredMethod("m1", Integer.class);
    ParamNameResolver resolver = new ParamNameResolver(new Configuration(), method, clazz);
    assertEquals(Integer.class, resolver.getType("p"));
    assertEquals(Integer.class, resolver.getType("param1"));
  }

  @Test
  void testGetTypeList() throws Exception {
    Class<?> clazz = A.class;
    Method method = clazz.getDeclaredMethod("m2", List.class);
    ParamNameResolver resolver = new ParamNameResolver(new Configuration(), method, clazz);
    assertEquals(List.class, ((ParameterizedType) resolver.getType("p")).getRawType());
    assertEquals(String.class, resolver.getType("p[0]"));
    assertEquals(String.class, resolver.getType("param1[0]"));
  }

  @Test
  void testGetTypeArray() throws Exception {
    Class<?> clazz = A.class;
    Method method = clazz.getDeclaredMethod("m3", Integer[].class);
    ParamNameResolver resolver = new ParamNameResolver(new Configuration(), method, clazz);
    assertEquals(Integer[].class, resolver.getType("p"));
    assertEquals(Integer.class, resolver.getType("p[0]"));
    assertEquals(Integer[].class, resolver.getType("param1"));
    assertEquals(Integer.class, resolver.getType("param1[0]"));
  }
}
