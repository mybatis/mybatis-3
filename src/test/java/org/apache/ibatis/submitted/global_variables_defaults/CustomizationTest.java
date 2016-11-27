/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.submitted.global_variables_defaults;

import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Property;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.type.JdbcType;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class CustomizationTest {

  @Test
  public void applyDefaultValueWhenCustomizeDefaultValueSeparator() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty(PropertyParser.KEY_DEFAULT_VALUE_SEPARATOR, "?:");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config-custom-separator.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();
    configuration.addMapper(CustomDefaultValueSeparatorMapper.class);

    SupportClasses.CustomCache cache = SupportClasses.Utils.unwrap(configuration.getCache(CustomDefaultValueSeparatorMapper.class.getName()));

    Assert.assertThat(configuration.getJdbcTypeForNull(), Is.is(JdbcType.NULL));
    Assert.assertThat(((UnpooledDataSource) configuration.getEnvironment().getDataSource()).getUrl(),
        Is.is("jdbc:hsqldb:mem:global_variables_defaults"));
    Assert.assertThat(configuration.getDatabaseId(), Is.is("hsql"));
    Assert.assertThat(((SupportClasses.CustomObjectFactory) configuration.getObjectFactory()).getProperties().getProperty("name"),
        Is.is("default"));
    Assert.assertThat(cache.getName(), Is.is("default"));

    SqlSession sqlSession = factory.openSession();
    try {
      CustomDefaultValueSeparatorMapper mapper = sqlSession.getMapper(CustomDefaultValueSeparatorMapper.class);
      Assert.assertThat(mapper.selectValue(null), Is.is("default"));
    } finally {
      sqlSession.close();
    }

  }

  @Test
  public void applyPropertyValueWhenCustomizeDefaultValueSeparator() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty(PropertyParser.KEY_DEFAULT_VALUE_SEPARATOR, "?:");
    props.setProperty("settings:jdbcTypeForNull", JdbcType.CHAR.name());
    props.setProperty("db:name", "global_variables_defaults_custom");
    props.setProperty("productName:hsql", "Hsql");
    props.setProperty("objectFactory:name", "customObjectFactory");
    props.setProperty("cache:name", "customCache");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config-custom-separator.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();
    configuration.addMapper(CustomDefaultValueSeparatorMapper.class);

    SupportClasses.CustomCache cache = SupportClasses.Utils.unwrap(configuration.getCache(CustomDefaultValueSeparatorMapper.class.getName()));

    Assert.assertThat(configuration.getJdbcTypeForNull(), Is.is(JdbcType.CHAR));
    Assert.assertThat(((UnpooledDataSource) configuration.getEnvironment().getDataSource()).getUrl(),
        Is.is("jdbc:hsqldb:mem:global_variables_defaults_custom"));
    Assert.assertThat(configuration.getDatabaseId(), IsNull.nullValue());
    Assert.assertThat(((SupportClasses.CustomObjectFactory) configuration.getObjectFactory()).getProperties().getProperty("name"),
        Is.is("customObjectFactory"));
    Assert.assertThat(cache.getName(), Is.is("customCache"));

    SqlSession sqlSession = factory.openSession();
    try {
      CustomDefaultValueSeparatorMapper mapper = sqlSession.getMapper(CustomDefaultValueSeparatorMapper.class);
      Assert.assertThat(mapper.selectValue("3333"), Is.is("3333"));
    } finally {
      sqlSession.close();
    }

  }

  @CacheNamespace(implementation = SupportClasses.CustomCache.class, properties = {
      @Property(name = "name", value = "${cache:name?:default}")
  })
  private interface CustomDefaultValueSeparatorMapper {
    @Select("SELECT '${val != null ? val : 'default'}' FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    String selectValue(@Param("val") String val);
  }

}
