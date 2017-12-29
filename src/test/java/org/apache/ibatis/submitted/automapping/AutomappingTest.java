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
package org.apache.ibatis.submitted.automapping;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AutomappingTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/automapping/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/automapping/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void shouldGetAUser() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      Assert.assertEquals("User1", user.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetAUserWhithPhoneNumber() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPhoneNumber(1);
      Assert.assertEquals("User1", user.getName());
      Assert.assertEquals(new Long(12345678901L), user.getPhone());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldNotInheritAutoMappingInherited_InlineNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_Inline(2);
      Assert.assertEquals(Integer.valueOf(2), user.getId());
      Assert.assertEquals("User2", user.getName());
      Assert.assertNull("should not inherit auto-mapping", user.getPets().get(0).getPetName());
      Assert.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void shouldNotInheritAutoMappingInherited_ExternalNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_External(2);
      Assert.assertEquals(Integer.valueOf(2), user.getId());
      Assert.assertEquals("User2", user.getName());
      Assert.assertNull("should not inherit auto-mapping", user.getPets().get(0).getPetName());
      Assert.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldIgnorePartialAutoMappingBehavior_InlineNestedResultMap() {
    // For nested resultMaps, PARTIAL works the same as NONE
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_Inline(2);
      Assert.assertEquals(Integer.valueOf(2), user.getId());
      Assert.assertEquals("User2", user.getName());
      Assert.assertNull("should not inherit auto-mapping", user.getPets().get(0).getPetName());
      Assert.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldRespectFullAutoMappingBehavior_InlineNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.FULL);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_Inline(2);
      Assert.assertEquals(Integer.valueOf(2), user.getId());
      Assert.assertEquals("User2", user.getName());
      Assert.assertEquals("Chien", user.getPets().get(0).getPetName());
      Assert.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldIgnorePartialAutoMappingBehavior_ExternalNestedResultMap() {
    // For nested resultMaps, PARTIAL works the same as NONE
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_External(2);
      Assert.assertEquals(Integer.valueOf(2), user.getId());
      Assert.assertEquals("User2", user.getName());
      Assert.assertNull("should not inherit auto-mapping", user.getPets().get(0).getPetName());
      Assert.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldRespectFullAutoMappingBehavior_ExternalNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.FULL);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_External(2);
      Assert.assertEquals(Integer.valueOf(2), user.getId());
      Assert.assertEquals("User2", user.getName());
      Assert.assertEquals("Chien", user.getPets().get(0).getPetName());
      Assert.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetBooks() {
    // set automapping to default partial
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      // no errors throw
      List<Book> books = mapper.getBooks();
      Assert.assertTrue("should return results,no errors throw", !books.isEmpty());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldUpdateFinalField() {
    // set automapping to default partial
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Article article = mapper.getArticle();
      // Java Language Specification 17.5.3 Subsequent Modification of Final Fields
      // http://docs.oracle.com/javase/specs/jls/se5.0/html/memory.html#17.5.3
      // The final field should be updated in mapping
      Assert.assertTrue("should update version in mapping", article.version > 0);
    } finally {
      sqlSession.close();
    }
  }
}
