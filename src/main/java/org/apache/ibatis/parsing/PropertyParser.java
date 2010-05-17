package org.apache.ibatis.parsing;

import java.util.Properties;

public class PropertyParser {

  public static String parse(String string, Properties variables) {
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    return parser.parse(string);
  }

  private static class VariableTokenHandler implements TokenHandler {
    private Properties variables;

    public VariableTokenHandler(Properties variables) {
      this.variables = variables;
    }

    public String handleToken(String content) {
      if (variables != null && variables.containsKey(content)) {
        return variables == null ? content : variables.getProperty(content);
      } else {
        return "${" + content + "}";
      }
    }
  }
}
