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

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class XmlMapperTest {

  @Test
  public void applyDefaultValueOnXmlMapper() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();
    configuration.addMapper(XmlMapper.class);
    SupportClasses.CustomCache cache = SupportClasses.Utils.unwrap(configuration.getCache(XmlMapper.class.getName()));

    Assert.assertThat(cache.getName(), Is.is("default"));

    SqlSession sqlSession = factory.openSession();
    try {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);

      Assert.assertThat(mapper.ping(), Is.is("Hello"));
      Assert.assertThat(mapper.selectOne(), Is.is("1"));
      Assert.assertThat(mapper.selectFromVariable(), Is.is("9999"));

    } finally {
      sqlSession.close();
    }

  }

  @Test
  public void applyPropertyValueOnXmlMapper() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty("ping.sql", "SELECT 'Hi' FROM INFORMATION_SCHEMA.SYSTEM_USERS");
    props.setProperty("cache.name", "custom");
    props.setProperty("select.columns", "'5555'");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();
    configuration.addMapper(XmlMapper.class);
    SupportClasses.CustomCache cache = SupportClasses.Utils.unwrap(configuration.getCache(XmlMapper.class.getName()));

    Assert.assertThat(cache.getName(), Is.is("custom"));

    SqlSession sqlSession = factory.openSession();
    try {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);

      Assert.assertThat(mapper.ping(), Is.is("Hi"));
      Assert.assertThat(mapper.selectOne(), Is.is("1"));
      Assert.assertThat(mapper.selectFromVariable(), Is.is("5555"));

    } finally {
      sqlSession.close();
    }

  }

  public interface XmlMapper {

    String ping();

    String selectOne();

    String selectFromVariable();

  }

}
