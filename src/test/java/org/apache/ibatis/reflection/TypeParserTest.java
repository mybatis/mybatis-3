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
package org.apache.ibatis.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TypeParserTest {
  private Class<?> resolve(String name) {
    try {
      return Resources.classForName(name);
    } catch (ClassNotFoundException exception) {
      throw new TypeException("Unresolved Java type: " + name, exception);
    }
  }

  static Stream<Type> types() {
    return Stream.of(new TypeReference<List<String>>() {
    }.getRawType(), new TypeReference<Map<String, List<Integer>>>() {
    }.getRawType(), new TypeReference<List<? extends Number>>() {
    }.getRawType(), new TypeReference<List<? super Integer>>() {
    }.getRawType(), new TypeReference<List<?>>() {
    }.getRawType(), new TypeReference<List<String>[]>() {
    }.getRawType(), new TypeReference<Map.Entry<String, Integer>>() {
    }.getRawType(), new TypeReference<Owner<String>.Child<Integer>>() {
    }.getRawType(), new TypeReference<Owner<String>.Leaf>() {
    }.getRawType(), String[][].class, int[][].class);
  }

  @ParameterizedTest
  @MethodSource("types")
  void roundTripsNestedTypesWildcardsArraysAndOwners(Type type) {
    Type parsed = TypeParser.parse(type.getTypeName(), this::resolve);
    assertEquals(type, parsed);
    assertEquals(type.hashCode(), parsed.hashCode());
  }

  @ParameterizedTest
  @ValueSource(strings = { "java.util.List<>", "java.util.List<java.lang.String, java.lang.Integer>",
      "java.util.Map<java.lang.String>", "java.util.List<java.lang.String>>", "java.util.List<java.lang.String",
      "java.util.List<int>", "java.util.List<? extends int>", "java.util.List<? super ?>", "java.util.List<?[]>", "?",
      "? extends java.lang.Number" })
  void rejectsMalformedTypesAndWrongArity(String description) {
    assertThrows(TypeException.class, () -> TypeParser.parse(description, this::resolve));
  }

  @ParameterizedTest
  @ValueSource(strings = { " ", "\t", "\n" })
  void acceptsWhitespaceBetweenWildcardKeywordsAndBounds(String whitespace) {
    assertEquals(new TypeReference<List<? extends Number>>() {
    }.getRawType(), TypeParser.parse("java.util.List<? extends" + whitespace + "java.lang.Number>", this::resolve));
    assertEquals(new TypeReference<List<? super Integer>>() {
    }.getRawType(), TypeParser.parse("java.util.List<? super" + whitespace + "java.lang.Integer>", this::resolve));
  }

  @ParameterizedTest
  @ValueSource(strings = { "java.util.List<? extends java.lang.Number>", "java.util.List<? super java.lang.Integer>" })
  void parsedTypesRemainStableAsCacheKeys(String description) {
    ParameterizedType parsed = (ParameterizedType) TypeParser.parse(description, this::resolve);
    Type equalType = TypeParser.parse(description, this::resolve);
    Map<Type, String> cache = new HashMap<>();
    cache.put(parsed, "handler");
    WildcardType wildcard = (WildcardType) parsed.getActualTypeArguments()[0];
    parsed.getActualTypeArguments()[0] = String.class;
    wildcard.getUpperBounds()[0] = String.class;
    if (wildcard.getLowerBounds().length > 0) {
      wildcard.getLowerBounds()[0] = String.class;
    }
    assertEquals(equalType, parsed);
    assertEquals("handler", cache.get(equalType));
  }

  @Test
  void typeImplementationsOwnTheirInputArrays() {
    Type[] arguments = { String.class };
    Type parameterized = new TypeParameterResolver.ParameterizedTypeImpl(List.class, null, arguments);
    arguments[0] = Integer.class;
    assertEquals(new TypeReference<List<String>>() {
    }.getRawType(), parameterized);

    Type[] lower = { Integer.class };
    Type[] upper = { Object.class };
    WildcardType wildcard = new TypeParameterResolver.WildcardTypeImpl(lower, upper);
    lower[0] = String.class;
    upper[0] = String.class;
    assertEquals(Integer.class, wildcard.getLowerBounds()[0]);
    assertEquals(Object.class, wildcard.getUpperBounds()[0]);
  }

  static class Owner<T> {
    class Child<U> {
    }

    class Leaf {
    }
  }
}
