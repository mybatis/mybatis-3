/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.oracle_implicit_cursor;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import java.util.function.Function;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.OracleTestContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TestcontainersTests")
class OracleImplicitCursorTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        OracleTestContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/oracle_implicit_cursor/CreateDB.sql");
  }

  @Test
  void shouldImplicitCursors_Statement() {
    doTest(Mapper::selectImplicitCursors_Statement);
  }

  @Test
  void shouldImplicitCursors_Prepared() {
    doTest(Mapper::selectImplicitCursors_Prepared);
  }

  @Test
  void shouldImplicitCursors_Callable() {
    doTest(Mapper::selectImplicitCursors_Callable);
  }

  private void doTest(Function<Mapper, List<Author>> query) {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Author> authors = query.apply(mapper);
      assertIterableEquals(
          List.of(new Author(1, "John", List.of(new Book(1, "C#"), new Book(2, "Python"), new Book(5, "Ruby"))),
              new Author(2, "Jane", List.of(new Book(3, "SQL"), new Book(4, "Java")))),
          authors);
    }
  }
}
