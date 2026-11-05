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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.LongTypeHandler;
import org.junit.jupiter.api.Test;

class XMLLanguageDriverTest {

  @Test
  void shouldPreserveParameterResolverForStaticScriptString() throws Exception {
    assertParameterResolver("<script>select #{id,jdbcType=BIGINT}</script>");
  }

  @Test
  void shouldPreserveParameterResolverForPlainSqlString() throws Exception {
    assertParameterResolver("select #{id,jdbcType=BIGINT}");
  }

  @Test
  void shouldStillAcceptScriptWithoutParameterResolver() {
    Configuration configuration = new Configuration();
    SqlSource source = new XMLLanguageDriver().createSqlSource(configuration,
        "<script>select #{value,jdbcType=BIGINT}</script>", Long.class);
    ParameterMapping mapping = source.getBoundSql(1L).getParameterMappings().get(0);
    assertThat(mapping.getTypeHandler()).isInstanceOf(LongTypeHandler.class);
  }

  private void assertParameterResolver(String script) throws Exception {
    Configuration configuration = new Configuration();
    Method method = Mapper.class.getMethod("select", Long.class);
    ParamNameResolver resolver = new ParamNameResolver(configuration, method, Mapper.class);
    XMLLanguageDriver driver = new XMLLanguageDriver();
    SqlSource stringSource = driver.createSqlSource(configuration, script, ParamMap.class, resolver);
    SqlSource nodeSource = driver.createSqlSource(configuration,
        new XPathParser(script.startsWith("<script>") ? script : "<script>" + script + "</script>").evalNode("/script"),
        ParamMap.class, resolver);
    ParameterMapping stringMapping = stringSource.getBoundSql(Map.of("id", 1L)).getParameterMappings().get(0);
    ParameterMapping nodeMapping = nodeSource.getBoundSql(Map.of("id", 1L)).getParameterMappings().get(0);
    assertThat(nodeMapping.getTypeHandler()).isInstanceOf(LongTypeHandler.class);
    assertThat(stringMapping.getTypeHandler()).isInstanceOf(LongTypeHandler.class);
    assertThat(stringMapping.getJavaType()).isEqualTo(nodeMapping.getJavaType());
  }

  interface Mapper {
    void select(@Param("id") Long id);
  }
}
