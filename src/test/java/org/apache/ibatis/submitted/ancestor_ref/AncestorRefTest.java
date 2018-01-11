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
package org.apache.ibatis.submitted.ancestor_ref;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class AncestorRefTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/ancestor_ref/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/ancestor_ref/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void testCircularAssociation() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserAssociation(1);
      assertEquals("User2", user.getFriend().getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testCircularCollection() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserCollection(2);
      assertEquals("User2", user.getFriends().get(0).getName());
      assertEquals("User3", user.getFriends().get(1).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testAncestorRef() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Blog blog = mapper.selectBlog(1);
      assertEquals("Author1", blog.getAuthor().getName());
      assertEquals("Author2", blog.getCoAuthor().getName());
      // author and coauthor should have a ref to blog
      assertEquals(blog, blog.getAuthor().getBlog());
      assertEquals(blog, blog.getCoAuthor().getBlog());
      // reputation should point to it author? or fail but do not point to a random one
      assertEquals(blog.getAuthor(), blog.getAuthor().getReputation().getAuthor());
      assertEquals(blog.getCoAuthor(), blog.getCoAuthor().getReputation().getAuthor());
    } finally {
      sqlSession.close();
    }
  }
}
