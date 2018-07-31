/**
 *    Copyright 2009-2018 the original author or authors.
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
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
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
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/empty_row/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/empty_row/CreateDB.sql");
  }

  @Before
  public void resetCallSettersOnNulls() {
    sqlSessionFactory.getConfiguration().setCallSettersOnNulls(false);
  }

  @Test
  public void shouldSimpleTypeBeNull() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      String result = mapper.getString();
      assertNull(result);
    }
  }

  @Test
  public void shouldObjectTypeNotBeNull() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getBean(1);
      assertNotNull(parent);
    }
  }

  @Test
  public void shouldMapBeEmpty() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, String> map = mapper.getMap(1);
      assertNotNull(map);
      assertTrue(map.isEmpty());
    }
  }

  @Test
  public void shouldMapHaveColumnNamesIfCallSettersOnNullsEnabled() {
    sqlSessionFactory.getConfiguration().setCallSettersOnNulls(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, String> map = mapper.getMap(1);
      assertEquals(2, map.size());
      assertTrue(map.containsKey("COL1"));
      assertTrue(map.containsKey("COL2"));
    }
  }

  @Test
  public void shouldAssociationNotBeNull() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getAssociation(1);
      assertNotNull(parent.getChild());
    }
  }

  @Test
  public void shouldAssociationBeNullIfNotNullColumnSpecified() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getAssociationWithNotNullColumn(1);
      assertNotNull(parent);
      assertNull(parent.getChild());
    }
  }

  @Test
  public void shouldNestedAssociationNotBeNull() {
    // #420
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getNestedAssociation();
      assertNotNull(parent.getChild().getGrandchild());
    }
  }

  @Test
  public void testCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getCollection(1);
      assertEquals(1, parent.getChildren().size());
      assertNotNull(parent.getChildren().get(0));
    }
  }

  @Test
  public void shouldSquashMultipleEmptyResults() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Parent parent = mapper.getTwoCollections(2);
      assertEquals(1, parent.getPets().size());
      assertNotNull(parent.getPets().get(0));
    }
  }
}
