/**
 *    Copyright 2009-2020 the original author or authors.
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

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class XmlMapperTest {

  @Test
  void applyDefaultValueOnXmlMapper() throws IOException {

    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables_defaults/mybatis-config.xml");
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, props);
    Configuration configuration = factory.getConfiguration();
    configuration.addMapper(XmlMapper.class);
    SupportClasses.CustomCache cache = SupportClasses.Utils.unwrap(configuration.getCache(XmlMapper.class.getName()));

    Assertions.assertThat(cache.getName()).isEqualTo("default");

    try (SqlSession sqlSession = factory.openSession()) {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);

      Assertions.assertThat(mapper.ping()).isEqualTo("Hello");
      Assertions.assertThat(mapper.selectOne()).isEqualTo("1");
      Assertions.assertThat(mapper.selectFromVariable()).isEqualTo("9999");
    }

  }

  @Test
  void applyPropertyValueOnXmlMapper() throws IOException {

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

    Assertions.assertThat(cache.getName()).isEqualTo("custom");

    try (SqlSession sqlSession = factory.openSession()) {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);

      Assertions.assertThat(mapper.ping()).isEqualTo("Hi");
      Assertions.assertThat(mapper.selectOne()).isEqualTo("1");
      Assertions.assertThat(mapper.selectFromVariable()).isEqualTo("5555");
    }

  }

  public interface XmlMapper {

    String ping();

    String selectOne();

    String selectFromVariable();

  }

}
