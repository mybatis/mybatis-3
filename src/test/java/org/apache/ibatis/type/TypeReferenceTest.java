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
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import org.junit.jupiter.api.Test;

class TypeReferenceTest {

  @Test
  void testRawType() {
    Type rawType = new TypeReference<List<Integer>>() {
    }.getRawType();
    assertTrue(rawType instanceof ParameterizedType);
    ParameterizedType parameterizedType = (ParameterizedType) rawType;
    assertEquals(List.class, parameterizedType.getRawType());
    assertEquals(1, parameterizedType.getActualTypeArguments().length);
    assertEquals(Integer.class, parameterizedType.getActualTypeArguments()[0]);
  }

  @Test
  void testWildcard() {
    Type rawType = new TypeReference<List<?>>() {
    }.getRawType();
    assertTrue(rawType instanceof ParameterizedType);
    ParameterizedType parameterizedType = (ParameterizedType) rawType;
    assertEquals(List.class, parameterizedType.getRawType());
    assertEquals(1, parameterizedType.getActualTypeArguments().length);
    Type arg = parameterizedType.getActualTypeArguments()[0];
    assertTrue(arg instanceof WildcardType);
    WildcardType wildcardType = (WildcardType) arg;
    assertEquals(1, wildcardType.getUpperBounds().length);
    assertEquals(Object.class, wildcardType.getUpperBounds()[0]);
    assertEquals(0, wildcardType.getLowerBounds().length);
  }
}
