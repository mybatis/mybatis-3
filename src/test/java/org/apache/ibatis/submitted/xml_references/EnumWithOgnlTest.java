/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.xml_references;

import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;

class EnumWithOgnlTest {

  @Test
  void testConfiguration() {
    UnpooledDataSourceFactory dataSourceFactory = new UnpooledDataSourceFactory();
    Properties dataSourceProperties = new Properties();
    dataSourceProperties.put("driver", "org.hsqldb.jdbcDriver");
    dataSourceProperties.put("url", "jdbc:hsqldb:mem:xml_references");
    dataSourceProperties.put("username", "sa");
    dataSourceFactory.setProperties(dataSourceProperties);
    Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSourceFactory.getDataSource());
    Configuration configuration = new Configuration();
    configuration.setEnvironment(environment);
    configuration.getTypeAliasRegistry().registerAlias(Person.class);
    configuration.addMapper(PersonMapper.class);
    configuration.addMapper(PersonMapper2.class);
    new DefaultSqlSessionFactory(configuration);
  }

  @Test
  void testMixedConfiguration() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/xml_references/ibatisConfig.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(PersonMapper2.class);
    }
  }
}
