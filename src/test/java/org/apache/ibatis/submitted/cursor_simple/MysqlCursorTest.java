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
package org.apache.ibatis.submitted.cursor_simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.MysqlContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TestcontainersTests")
class MysqlCursorTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        MysqlContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/cursor_simple/CreateDB.sql");
  }

  @Test
  void testMySqlStreamResultSet() throws IOException {
    // #1654 and https://bugs.mysql.com/bug.php?id=96786
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        Cursor<User> cursor = mapper.getUsersMysqlStream();
        Iterator<User> iterator = cursor.iterator();
        User user = iterator.next();
        assertEquals("User1", user.getName());
        cursor.close();
      }
    }
  }

  @Test
  void testMySqlStreamResultSetBatch() throws IOException {
    // #1654 and https://bugs.mysql.com/bug.php?id=96786
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        Cursor<User> cursor = mapper.getUsersMysqlStream();
        Iterator<User> iterator = cursor.iterator();
        User user = iterator.next();
        assertEquals("User1", user.getName());
        cursor.close();
      }
    }
  }
}
