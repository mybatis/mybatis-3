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
package org.apache.ibatis.submitted.global_variables_defaults;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.JdbcType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

  @Test
  void applyDefaultValueOnXmlConfiguration() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");

    Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();

    Assertions.assertThat(configuration.getJdbcTypeForNull()).isEqualTo(JdbcType.NULL);
    Assertions.assertThat(((UnpooledDataSource) configuration.getEnvironment().getDataSource()).getUrl())
        .isEqualTo("jdbc:hsqldb:mem:global_variables_defaults");
    Assertions.assertThat(configuration.getDatabaseId()).isEqualTo("hsql");
    Assertions
        .assertThat(
            ((SupportClasses.CustomObjectFactory) configuration.getObjectFactory()).getProperties().getProperty("name"))
        .isEqualTo("default");

  }

  @Test
  void applyPropertyValueOnXmlConfiguration() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty("settings.jdbcTypeForNull", JdbcType.CHAR.name());
    props.setProperty("db.name", "global_variables_defaults_custom");
    props.setProperty("productName.hsql", "Hsql");
    props.setProperty("objectFactory.name", "custom");

    Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();

    Assertions.assertThat(configuration.getJdbcTypeForNull()).isEqualTo(JdbcType.CHAR);
    Assertions.assertThat(((UnpooledDataSource) configuration.getEnvironment().getDataSource()).getUrl())
        .isEqualTo("jdbc:hsqldb:mem:global_variables_defaults_custom");
    Assertions.assertThat(configuration.getDatabaseId()).isNull();
    Assertions
        .assertThat(
            ((SupportClasses.CustomObjectFactory) configuration.getObjectFactory()).getProperties().getProperty("name"))
        .isEqualTo("custom");

  }

  @Test
  void testAmbiguityCache() {
    Configuration configuration = new Configuration();

    configuration.addMappedStatement(
        new MappedStatement.Builder(configuration, "org.apache.ibatis.submitted.DemoMapper1.selectById",
            new StaticSqlSource(configuration, "select * from test where id = 1"), SqlCommandType.SELECT).build());
    configuration
        .addMappedStatement(new MappedStatement.Builder(configuration, "org.apache.ibatis.submitted.DemoMapper1.test",
            new StaticSqlSource(configuration, "select * from test"), SqlCommandType.SELECT).build());
    configuration
        .addMappedStatement(new MappedStatement.Builder(configuration, "org.apache.ibatis.submitted.DemoMapper2.test",
            new StaticSqlSource(configuration, "select * from test"), SqlCommandType.SELECT).build());

    Assertions.assertThat(configuration.getMappedStatement("selectById")).isNotNull();
    Assertions.assertThat(configuration.getMappedStatement("org.apache.ibatis.submitted.DemoMapper1.test")).isNotNull();
    Assertions.assertThat(configuration.getMappedStatement("org.apache.ibatis.submitted.DemoMapper2.test")).isNotNull();

    Assertions.assertThatThrownBy(() -> configuration.getMappedStatement("test"))
        .isInstanceOf(IllegalArgumentException.class).hasMessage(
            "test is ambiguous in Mapped Statements collection (try using the full name including the namespace, or rename one of the entries)");
  }

}
