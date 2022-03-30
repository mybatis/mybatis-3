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
package org.apache.ibatis.submitted.postgres_genkeys;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.PgContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TestcontainersTests")
class PostgresGeneratedKeysTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        PgContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.setUseGeneratedKeys(true);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/postgres_genkeys/CreateDB.sql");
  }

  @Test
  void testInsertIntoTableWithNoSerialColumn() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Section section = new Section();
      section.setSectionId(2);
      section.setName("Section 2");
      int result = mapper.insertSection(section);
      assertEquals(1, result);
    }
  }

  @Test
  void testUpdateTableWithSerial() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setUserId(1);
      user.setName("Ethan");
      int result = mapper.updateUser(user);
      assertEquals(1, result);
    }
  }

  @Test
  void testUnusedGeneratedKey() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      int result = mapper.insertUser("John");
      assertEquals(1, result);
    }
  }
}
