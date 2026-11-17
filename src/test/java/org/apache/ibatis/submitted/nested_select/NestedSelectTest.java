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
package org.apache.ibatis.submitted.nested_select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.PgContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TestcontainersTests")
class NestedSelectTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        PgContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/nested_select/CreateDB.sql");
  }

  @Test
  void keyColumnWithNoAssignedHandler() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      PersistenceException ex = Assertions.assertThrows(PersistenceException.class,
          () -> mapper.selectPersonWithPets(1));
      assertEquals(ReflectionException.class, ex.getCause().getClass());
    }
  }
}
