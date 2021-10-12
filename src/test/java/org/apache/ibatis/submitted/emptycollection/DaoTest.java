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
package org.apache.ibatis.submitted.emptycollection;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DaoTest {
  private Connection conn;
  private Dao dao;
  private SqlSession sqlSession;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/emptycollection/mybatis-config.xml")) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSession = sqlSessionFactory.openSession();
    }
    conn = sqlSession.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    dao = sqlSession.getMapper(Dao.class);
  }

  @AfterEach
  void tearDown() throws Exception {
    conn.close();
    sqlSession.close();
  }

  @Test
  void testWithEmptyList() {
    final List<TodoLists> actual = dao.selectWithEmptyList();
    Assertions.assertEquals(1, actual.size());
    final List<TodoItem> todoItems = actual.get(0).getTodoItems();
    Assertions.assertEquals(0, todoItems.size(), "expect " + todoItems + " to be empty");
  }

  @Test
  void testWithNonEmptyList() {
    final List<TodoLists> actual = dao.selectWithNonEmptyList();
    checkNonEmptyList(actual);
  }

  @Test
  void testWithNonEmptyList_noCollectionId() {
    final List<TodoLists> actual = dao.selectWithNonEmptyList_noCollectionId();

    checkNonEmptyList(actual);
  }

  private void checkNonEmptyList(final List<TodoLists> actual) {
//  Assertions.assertEquals("[List(1)=[a description(1), a 2nd description(2)], List(2)=[a description(1)]]", actual.toString());
    Assertions.assertEquals(2, actual.size());

    Assertions.assertEquals(2, actual.get(0).getTodoItems().size());
    Assertions.assertEquals(1, actual.get(0).getTodoItems().get(0).getOrder());
    Assertions.assertEquals("a description", actual.get(0).getTodoItems().get(0).getDescription().trim());
    Assertions.assertEquals(2, actual.get(0).getTodoItems().get(1).getOrder());
    Assertions.assertEquals("a 2nd description", actual.get(0).getTodoItems().get(1).getDescription().trim());

    Assertions.assertEquals(1, actual.get(1).getTodoItems().size());
    Assertions.assertEquals(1, actual.get(1).getTodoItems().get(0).getOrder());
    Assertions.assertEquals("a description", actual.get(0).getTodoItems().get(0).getDescription().trim());

    // We should have gotten three item objects. The first item from the first list and the first item from
    // the second list have identical properties, but they should be distinct objects
    Assertions.assertNotSame(actual.get(0).getTodoItems().get(0), actual.get(1).getTodoItems().get(0));
  }
}
