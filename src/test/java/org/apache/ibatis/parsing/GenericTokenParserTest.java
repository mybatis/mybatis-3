/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.parsing;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GenericTokenParserTest {

  public static class VariableTokenHandler implements TokenHandler {
    private Map<String, String> variables = new HashMap<String, String>();

    public VariableTokenHandler(Map<String, String> variables) {
      this.variables = variables;
    }

    public String handleToken(String content) {
      return variables.get(content);
    }
  }

  @Test
  public void shouldDemonstrateGenericTokenReplacement() {
    GenericTokenParser parser = new GenericTokenParser("${", "}", new VariableTokenHandler(new HashMap<String, String>() {
      {
        put("first_name", "James");
        put("initial", "T");
        put("last_name", "Kirk");
        put("", "");
      }
    }));

    assertEquals("James T Kirk reporting.", parser.parse("${first_name} ${initial} ${last_name} reporting."));
    assertEquals("Hello captain James T Kirk", parser.parse("Hello captain ${first_name} ${initial} ${last_name}"));
    assertEquals("James T Kirk", parser.parse("${first_name} ${initial} ${last_name}"));
    assertEquals("JamesTKirk", parser.parse("${first_name}${initial}${last_name}"));
    assertEquals("{}JamesTKirk", parser.parse("{}${first_name}${initial}${last_name}"));
    assertEquals("}JamesTKirk", parser.parse("}${first_name}${initial}${last_name}"));

    assertEquals("}James{{T}}Kirk", parser.parse("}${first_name}{{${initial}}}${last_name}"));
    assertEquals("}James}T{Kirk", parser.parse("}${first_name}}${initial}{${last_name}"));
    assertEquals("}James}T{Kirk", parser.parse("}${first_name}}${initial}{${last_name}"));
    assertEquals("}James}T{Kirk{{}}", parser.parse("}${first_name}}${initial}{${last_name}{{}}"));
    assertEquals("}James}T{Kirk{{}}", parser.parse("}${first_name}}${initial}{${last_name}{{}}${}"));

    assertEquals("{$$something}JamesTKirk", parser.parse("{$$something}${first_name}${initial}${last_name}"));
    assertEquals("${", parser.parse("${"));
    assertEquals("}", parser.parse("}"));
    assertEquals("Hello ${ this is a test.", parser.parse("Hello ${ this is a test."));
    assertEquals("Hello } this is a test.", parser.parse("Hello } this is a test."));
    assertEquals("Hello } ${ this is a test.", parser.parse("Hello } ${ this is a test."));
  }

  @Test
  public void shallNotInterpolateSkippedVaiables() {
    GenericTokenParser parser = new GenericTokenParser("${", "}", new VariableTokenHandler(new HashMap<String, String>()));

    assertEquals("${skipped} variable", parser.parse("\\${skipped} variable"));
    assertEquals("This is a ${skipped} variable", parser.parse("This is a \\${skipped} variable"));
    assertEquals("null ${skipped} variable", parser.parse("${skipped} \\${skipped} variable"));
    assertEquals("The null is ${skipped} variable", parser.parse("The ${skipped} is \\${skipped} variable"));
  }

  @Test(timeout = 1000)
  public void shouldParseFastOnJdk7u6() {
    // issue #760
    GenericTokenParser parser = new GenericTokenParser("${", "}", new VariableTokenHandler(new HashMap<String, String>() {
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
  }

}
