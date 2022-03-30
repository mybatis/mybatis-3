/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.keycolumn;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.PgContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author Jeff Butler
 */
@Tag("TestcontainersTests")
class InsertTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        PgContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(InsertMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/keycolumn/CreateDB.sql");
  }

  @Test
  void testInsertAnnotated() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");

      int rows = mapper.insertNameAnnotated(name);

      assertNotNull(name.getId());
      assertEquals(1, rows);
    }
  }

  @Test
  void testInsertMapped() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");

      int rows = mapper.insertNameMapped(name);

      assertNotNull(name.getId());
      assertEquals(1, rows);
    }
  }

  @Test
  void testInsertMappedBatch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");
      mapper.insertNameMapped(name);
      Name name2 = new Name();
      name2.setFirstName("Wilma");
      name2.setLastName("Flintstone");
      mapper.insertNameMapped(name2);
      List<BatchResult> batchResults = sqlSession.flushStatements();
      assertNotNull(name.getId());
      assertNotNull(name2.getId());
      assertEquals(1, batchResults.size());
    }
  }

}
