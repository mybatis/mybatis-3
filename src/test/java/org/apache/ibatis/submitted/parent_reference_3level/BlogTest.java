/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.parent_reference_3level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.Assert;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class BlogTest {

  protected SqlSessionFactory sqlSessionFactory;

  protected String getConfigPath() {
    return "org/apache/ibatis/submitted/parent_reference_3level/mybatis-config.xml";
  }

  @Before
  public void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:parent_reference_3level", "sa", "");
      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/parent_reference_3level/CreateDB.sql");
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader(getConfigPath());
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();

    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testSelectBlogWithPosts() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      Blog result = mapper.selectBlogByPrimaryKey(1);
      assertNotNull(result);
      assertEquals("Blog with posts", result.getTitle());
      Assert.assertEquals(2, result.getPosts().size());
      Post firstPost = result.getPosts().get(0);
      Assert.assertEquals(2, firstPost.getComments().size());
      Post secondPost = result.getPosts().get(1);
      Assert.assertEquals(1, secondPost.getComments().size());
    } finally {
      session.close();
    }
  }

  @Test
  public void testSelectBlogWithoutPosts() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      Blog result = mapper.selectBlogByPrimaryKey(2);
      assertNotNull(result);
      assertEquals("Blog without posts", result.getTitle());
      Assert.assertEquals(0, result.getPosts().size());
    } finally {
      session.close();
    }
  }
}
