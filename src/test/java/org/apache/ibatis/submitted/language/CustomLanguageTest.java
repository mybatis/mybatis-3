package org.apache.ibatis.submitted.language;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.scripting.xmltags.ExpressionEvaluator;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Liu Dongmiao
 */
public class CustomLanguageTest {

  @Test
  public void checkXml() throws IOException {
    Configuration configuration1 = new Configuration();
    Map<String, Object> parameter = new LinkedHashMap<>();
    parameter.put("a", "a");
    parameter.put("b", Collections.singletonList("0"));
    parameter.put("c", "1");
    String sql1 = getSql(configuration1, parameter);

    Configuration configuration2 = new Configuration();
    configuration2.getLanguageRegistry().setDefaultDriverClass(CustomXMLLanguageDriver.class);
    CustomXMLLanguageDriver.resetDollars();
    String sql2 = getSql(configuration2, null);
    Set<String> dollars = CustomXMLLanguageDriver.getDollars();
    Assert.assertTrue(dollars.contains("c"));
    Assert.assertEquals(sql1, sql2);
  }

  protected String getSql(Configuration configuration, Object parameter) throws IOException {
    try (InputStream inputStream = Resources.getResourceAsStream("org/apache/ibatis/submitted/language/CustomLanguageMapper.xml")) {
      XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration, "CustomLanguageMapper.xml", configuration.getSqlFragments());
      xmlMapperBuilder.parse();
    }
    return configuration.getMappedStatement("select").getBoundSql(parameter).getSql();
  }

  public static class CustomXMLLanguageDriver extends XMLLanguageDriver {

    static final ThreadLocal<Set<String>> DOLLARS = ThreadLocal.withInitial(LinkedHashSet::new);

    public CustomXMLLanguageDriver() {
      super();
    }

    static void resetDollars() {
      DOLLARS.get().clear();
    }

    static Set<String> getDollars() {
      return DOLLARS.get();
    }

    protected Object getOgnlValue(String expression, Map<String, Object> bindings) {
      DOLLARS.get().add(expression);
      return "1";
    }

    protected ExpressionEvaluator newExpressionEvaluator() {
      return new ExpressionEvaluator() {
        @Override
        public boolean evaluateBoolean(String expression, Object parameterObject) {
          return true;
        }

        public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
          return Collections.singletonList("0");
        }
      };
    }
  }
}
