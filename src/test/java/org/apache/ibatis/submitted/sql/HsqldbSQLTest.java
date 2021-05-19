/*
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
package org.apache.ibatis.submitted.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HsqldbSQLTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:HsqldbSQLTest", "sa", ""));
    configuration.setEnvironment(environment);
    configuration.setUseGeneratedKeys(true);
    configuration.addMapper(Mapper.class);
    Properties properties = new Properties();
    properties.setProperty("schema", "");
    configuration.setVariables(properties);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/sql/CreateDB-hsqldb.sql");
  }

  @Test
  void testFetchFirst() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        List<User> users = mapper.findAll(0, 2);
        assertEquals(2, users.size());
        assertEquals("Jimmy", users.get(0).getName());
        assertEquals("Iwao", users.get(1).getName());
      }
      {
        List<User> users = mapper.findAll(1, 2);
        assertEquals(2, users.size());
        assertEquals("Iwao", users.get(0).getName());
        assertEquals("Kazuki", users.get(1).getName());
      }
      {
        List<User> users = mapper.findAll(2, 2);
        assertEquals(1, users.size());
        assertEquals("Kazuki", users.get(0).getName());
      }
    }
  }

}
