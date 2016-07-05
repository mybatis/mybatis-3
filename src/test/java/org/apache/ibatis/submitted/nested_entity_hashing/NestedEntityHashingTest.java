/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.nested_entity_hashing;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NestedEntityHashingTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested_entity_hashing/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested_entity_hashing/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void shouldGet3DistinctObjects() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Parent> parents = mapper.getWithAssociation();
      for (Parent parent: parents) {
          Assert.assertNotNull(parent);
          Assert.assertNotNull(parent.getChild());
      }
      Assert.assertEquals(3, parents.size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGet2Objects() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Parent> parents = mapper.getWithCollection();
      for (Parent parent: parents) {
          Assert.assertNotNull(parent);
          Assert.assertFalse(parent.getChildren().isEmpty());
      }
      Assert.assertEquals(2, parents.size());
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void shouldGetNObjects() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Parent> parents = mapper.getWithAssociationAndCollection();
      for (Parent parent: parents) {
          Assert.assertNotNull(parent);
          Assert.assertFalse(parent.getChildren().isEmpty());
      }
      Assert.assertEquals(3, parents.size());
      Assert.assertEquals(2, parents.get(0).getChildren().size());
      Assert.assertEquals(2, parents.get(1).getChildren().size());
      Assert.assertEquals(1, parents.get(2).getChildren().size());
      System.out.println();
    } finally {
      sqlSession.close();
    }
  }

}
