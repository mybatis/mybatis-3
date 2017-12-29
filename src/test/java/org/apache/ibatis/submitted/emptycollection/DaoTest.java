/**
 *    Copyright 2009-2017 the original author or authors.
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DaoTest {
  private Connection conn;
  private Dao dao;
  private SqlSession sqlSession;

  @Before
  public void setUp() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/emptycollection/mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    sqlSession = sqlSessionFactory.openSession();
    conn = sqlSession.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    dao = sqlSession.getMapper(Dao.class);
  }

  @After
  public void tearDown() throws Exception {
    conn.close();
    sqlSession.close();
  }

  @Test
  public void testWithEmptyList() throws Exception {
    final List<TodoLists> actual = dao.selectWithEmptyList();
    Assert.assertEquals(1, actual.size());
    final List<TodoItem> todoItems = actual.get(0).getTodoItems();
    Assert.assertEquals("expect " + todoItems + " to be empty", 0, todoItems.size());        
  }

  @Test
  public void testWithNonEmptyList() throws Exception {
    final List<TodoLists> actual = dao.selectWithNonEmptyList();
    checkNonEmptyList(actual);
  }

  @Test
  public void testWithNonEmptyList_noCollectionId() throws Exception {
    final List<TodoLists> actual = dao.selectWithNonEmptyList_noCollectionId();

    checkNonEmptyList(actual);
  }

  private void checkNonEmptyList(final List<TodoLists> actual) {
//  Assert.assertEquals("[List(1)=[a description(1), a 2nd description(2)], List(2)=[a description(1)]]", actual.toString());
    Assert.assertEquals(2, actual.size());

    Assert.assertEquals(2, actual.get(0).getTodoItems().size());
    Assert.assertEquals(1, actual.get(0).getTodoItems().get(0).getOrder());
    Assert.assertEquals("a description", actual.get(0).getTodoItems().get(0).getDescription().trim());
    Assert.assertEquals(2, actual.get(0).getTodoItems().get(1).getOrder());
    Assert.assertEquals("a 2nd description", actual.get(0).getTodoItems().get(1).getDescription().trim());

    Assert.assertEquals(1, actual.get(1).getTodoItems().size());
    Assert.assertEquals(1, actual.get(1).getTodoItems().get(0).getOrder());
    Assert.assertEquals("a description", actual.get(0).getTodoItems().get(0).getDescription().trim());

    // We should have gotten three item objects. The first item from the first list and the first item from
    // the second list have identical properties, but they should be distinct objects
    Assert.assertNotSame(actual.get(0).getTodoItems().get(0), actual.get(1).getTodoItems().get(0));
  }
}