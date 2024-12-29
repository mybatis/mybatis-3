/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GenericTokenParserTest {

  public static class VariableTokenHandler implements TokenHandler {
    private Map<String, String> variables = new HashMap<>();

    VariableTokenHandler(Map<String, String> variables) {
      this.variables = variables;
    }

    @Override
    public String handleToken(String content) {
      return variables.get(content);
    }
  }

  @ParameterizedTest
  @MethodSource("shouldDemonstrateGenericTokenReplacementProvider")
  void shouldDemonstrateGenericTokenReplacement(String expected, String text) {
    GenericTokenParser parser = new GenericTokenParser("${", "}",
        new VariableTokenHandler(new HashMap<String, String>() {
          private static final long serialVersionUID = 1L;

          {
            put("first_name", "James");
            put("initial", "T");
            put("last_name", "Kirk");
            put("var{with}brace", "Hiya");
            put("", "");
          }
        }));
    assertEquals(expected, parser.parse(text));
  }

  static Stream<Arguments> shouldDemonstrateGenericTokenReplacementProvider() {
    return Stream.of(arguments("James T Kirk reporting.", "${first_name} ${initial} ${last_name} reporting."),
        arguments("Hello captain James T Kirk", "Hello captain ${first_name} ${initial} ${last_name}"),
        arguments("James T Kirk", "${first_name} ${initial} ${last_name}"),
        arguments("JamesTKirk", "${first_name}${initial}${last_name}"),
        arguments("{}JamesTKirk", "{}${first_name}${initial}${last_name}"),
        arguments("}JamesTKirk", "}${first_name}${initial}${last_name}"),

        arguments("}James{{T}}Kirk", "}${first_name}{{${initial}}}${last_name}"),
        arguments("}James}T{Kirk", "}${first_name}}${initial}{${last_name}"),
        arguments("}James}T{Kirk", "}${first_name}}${initial}{${last_name}"),
        arguments("}James}T{Kirk{{}}", "}${first_name}}${initial}{${last_name}{{}}"),
        arguments("}James}T{Kirk{{}}", "}${first_name}}${initial}{${last_name}{{}}${}"),

        arguments("{$$something}JamesTKirk", "{$$something}${first_name}${initial}${last_name}"), arguments("${", "${"),
        arguments("${\\}", "${\\}"), arguments("Hiya", "${var{with\\}brace}"), arguments("", "${}"),
        arguments("}", "}"), arguments("Hello ${ this is a test.", "Hello ${ this is a test."),
        arguments("Hello } this is a test.", "Hello } this is a test."),
        arguments("Hello } ${ this is a test.", "Hello } ${ this is a test."));
  }

  @ParameterizedTest
  @MethodSource("shallNotInterpolateSkippedVariablesProvider")
  void shallNotInterpolateSkippedVariables(String expected, String text) {
    GenericTokenParser parser = new GenericTokenParser("${", "}", new VariableTokenHandler(new HashMap<>()));
    assertEquals(expected, parser.parse(text));
  }

  static Stream<Arguments> shallNotInterpolateSkippedVariablesProvider() {
    return Stream.of(arguments("${skipped} variable", "\\${skipped} variable"),
        arguments("This is a ${skipped} variable", "This is a \\${skipped} variable"),
        arguments("null ${skipped} variable", "${skipped} \\${skipped} variable"),
        arguments("The null is ${skipped} variable", "The ${skipped} is \\${skipped} variable"));
  }

  @Disabled("Because it randomly fails on Github CI. It could be useful during development.")
  @Test
  void shouldParseFastOnJdk7u6() {
    Assertions.assertTimeout(Duration.ofMillis(1000), () -> {
      // issue #760
      GenericTokenParser parser = new GenericTokenParser("${", "}",
          new VariableTokenHandler(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
              put("first_name", "James");
              put("initial", "T");
              put("last_name", "Kirk");
              put("", "");
            }
          }));

      StringBuilder input = new StringBuilder();
      for (int i = 0; i < 10000; i++) {
        input.append("${first_name} ${initial} ${last_name} reporting. ");
      }
      StringBuilder expected = new StringBuilder();
      for (int i = 0; i < 10000; i++) {
        expected.append("James T Kirk reporting. ");
      }
      assertEquals(expected.toString(), parser.parse(input.toString()));
    });
  }

}
