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

package org.apache.ibatis.scripting.xmltags;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OgnlExpressionParserTest {

  @Test
  void concurrentAccess() throws Exception {
    OgnlExpressionParser parser = new OgnlExpressionParser(new Configuration());
    class DataClass {
      @SuppressWarnings("unused")
      private int id;
    }
    int run = 1000;
    Map<String, Object> context = new HashMap<>();
    List<Future<Object>> futures = new ArrayList<>();
    context.put("data", new DataClass());
    ExecutorService executor = Executors.newCachedThreadPool();
    IntStream.range(0, run).forEach(i -> futures.add(executor.submit(() -> parser.getValue("data.id", context))));
    for (int i = 0; i < run; i++) {
      assertNotNull(futures.get(i).get());
    }
    executor.shutdown();
  }

  @Test
  void issue2609() {
    OgnlExpressionParser parser = new OgnlExpressionParser(new Configuration());
    Map<String, Object> context = new HashMap<>();
    context.put("d1", Date.valueOf("2022-01-01"));
    context.put("d2", Date.valueOf("2022-01-02"));
    assertEquals(-1, parser.getValue("d1.compareTo(d2)", context));
  }

  enum DRINK {
    COFFEE, TEA
  }

  public interface HasCode {
    int code();
  }

  enum DRINK2 implements HasCode {
    COFFEE {
      @Override
      public int code() {
        return 1;
      }
    },
    TEA {
      @Override
      public int code() {
        return 2;
      }
    };
  }

  public static class Foo {
    public static final String STR = "yowza";

    String bar() {
      return "BAR!";
    }
  }

  @MethodSource
  @ParameterizedTest
  void shouldResolveTypeAlias(Object expected, Class<?> aliasedClass, String expression) {
    Configuration configuration = new Configuration();
    TypeAliasRegistry typeAliasRegistry = configuration.getTypeAliasRegistry();
    typeAliasRegistry.registerAlias(aliasedClass);

    OgnlExpressionParser parser = new OgnlExpressionParser(configuration);
    Map<String, Object> context = new HashMap<>();
    assertEquals(expected, parser.getValue(expression, context));
  }

  static Stream<Arguments> shouldResolveTypeAlias() {
    return Stream.of(Arguments.arguments("yowza", Foo.class, "@Foo@STR"),
        Arguments.arguments("yowza", Foo.class, "@foo@STR"), Arguments.arguments("BAR!", Foo.class, "new Foo().bar()"),
        Arguments.arguments("BAR!", Foo.class, "new foo().bar()"),
        Arguments.arguments("COFFEE", DRINK.class, "@DRINK@COFFEE.name()"),
        Arguments.arguments(2, DRINK2.class, "@DRINK2@TEA.code()"));
  }

  @Test
  void shouldThrowIfClassNotFound() {
    OgnlExpressionParser parser = new OgnlExpressionParser(new Configuration());
    Map<String, Object> context = new HashMap<>();
    assertThatThrownBy(() -> parser.getValue("@foo@BAR", context)).isInstanceOf(TypeException.class)
        .hasCauseInstanceOf(ClassNotFoundException.class);
  }

  @Test
  @Disabled("In MyBatis, 'int' is an alias for 'java.lang.Integer', but OGNL does not use class resolver for primitives, it seems.")
  void typeAliasCornerCases_int() {
    OgnlExpressionParser parser = new OgnlExpressionParser(new Configuration());
    Map<String, Object> context = new HashMap<>();
    // This works.
    assertEquals(Integer.MAX_VALUE, parser.getValue("@integer@MAX_VALUE", context));
    // This does not.
    assertEquals(Integer.MAX_VALUE, parser.getValue("@int@MAX_VALUE", context));
  }

  @Test
  @Disabled("In MyBatis, 'boolean' is an alias for 'java.lang.Boolean', but OGNL does not use class resolver for primitives, it seems.")
  void typeAliasCornerCases_boolean() {
    OgnlExpressionParser parser = new OgnlExpressionParser(new Configuration());
    Map<String, Object> context = new HashMap<>();
    // This works.
    assertEquals(Boolean.TRUE, parser.getValue("@Boolean@MTRUE", context));
    // This does not.
    assertEquals(Boolean.TRUE, parser.getValue("@boolean@TRUE", context));
  }
}
