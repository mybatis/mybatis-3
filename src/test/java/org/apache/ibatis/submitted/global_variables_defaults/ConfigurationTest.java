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

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;
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

public class ConfigurationTest {

  @Test
  public void applyDefaultValueOnXmlConfiguration() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();

    Assert.assertThat(configuration.getJdbcTypeForNull(), Is.is(JdbcType.NULL));
    Assert.assertThat(((UnpooledDataSource) configuration.getEnvironment().getDataSource()).getUrl(),
        Is.is("jdbc:hsqldb:mem:global_variables_defaults"));
    Assert.assertThat(configuration.getDatabaseId(), Is.is("hsql"));
    Assert.assertThat(((SupportClasses.CustomObjectFactory) configuration.getObjectFactory()).getProperties().getProperty("name"),
        Is.is("default"));

  }

  @Test
  public void applyPropertyValueOnXmlConfiguration() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty("settings.jdbcTypeForNull", JdbcType.CHAR.name());
    props.setProperty("db.name", "global_variables_defaults_custom");
    props.setProperty("productName.hsql", "Hsql");
    props.setProperty("objectFactory.name", "custom");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();

    Assert.assertThat(configuration.getJdbcTypeForNull(), Is.is(JdbcType.CHAR));
    Assert.assertThat(((UnpooledDataSource) configuration.getEnvironment().getDataSource()).getUrl(),
        Is.is("jdbc:hsqldb:mem:global_variables_defaults_custom"));
    Assert.assertThat(configuration.getDatabaseId(), IsNull.nullValue());
    Assert.assertThat(((SupportClasses.CustomObjectFactory) configuration.getObjectFactory()).getProperties().getProperty("name"),
        Is.is("custom"));

  }

}
