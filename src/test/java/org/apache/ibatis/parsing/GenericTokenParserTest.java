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

}
