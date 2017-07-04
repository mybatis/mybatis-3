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
package org.apache.ibatis.submitted.empty_row;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReturnInstanceForEmptyRowTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/empty_row/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/empty_row/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Before
  public void resetCallSettersOnNulls() {
    sqlSessionFactory.getConfiguration().setCallSettersOnNulls(false);
  }

  @Test
  public void shouldSimpleTypeBeNull() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      String result = mapper.getString();
      assertNull(result);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldObjectTypeNotBeNull() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getBean(1);
      assertNotNull(parent);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldMapBeEmpty() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, String> map = mapper.getMap(1);
      assertNotNull(map);
      assertTrue(map.isEmpty());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldMapHaveColumnNamesIfCallSettersOnNullsEnabled() {
    sqlSessionFactory.getConfiguration().setCallSettersOnNulls(true);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, String> map = mapper.getMap(1);
      assertEquals(2, map.size());
      assertTrue(map.containsKey("COL1"));
      assertTrue(map.containsKey("COL2"));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldAssociationNotBeNull() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getAssociation(1);
      assertNotNull(parent.getChild());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldAssociationBeNullIfNotNullColumnSpecified() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getAssociationWithNotNullColumn(1);
      assertNotNull(parent);
      assertNull(parent.getChild());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldNestedAssociationNotBeNull() {
    // #420
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getNestedAssociation();
      assertNotNull(parent.getChild().getGrandchild());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testCollection() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getCollection(1);
      assertEquals(1, parent.getChildren().size());
      assertNotNull(parent.getChildren().get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldSquashMultipleEmptyResults() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getTwoCollections(2);
      assertEquals(1, parent.getPets().size());
      assertNotNull(parent.getPets().get(0));
    } finally {
      sqlSession.close();
    }
  }
}
