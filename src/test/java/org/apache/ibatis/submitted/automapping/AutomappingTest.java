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
package org.apache.ibatis.submitted.automapping;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutomappingTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/automapping/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/automapping/CreateDB.sql");
  }

  @Test
  void shouldGetAUser() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      Assertions.assertEquals("User1", user.getName());
    }
  }

  @Test
  void shouldGetAUserWhithPhoneNumber() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPhoneNumber(1);
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals(Long.valueOf(12345678901L), user.getPhone());
    }
  }

  @Test
  void shouldNotInheritAutoMappingInherited_InlineNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_Inline(2);
      Assertions.assertEquals(Integer.valueOf(2), user.getId());
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertNull(user.getPets().get(0).getPetName(), "should not inherit auto-mapping");
      Assertions.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    }
  }

  @Test
  void shouldNotInheritAutoMappingInherited_ExternalNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.NONE);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_External(2);
      Assertions.assertEquals(Integer.valueOf(2), user.getId());
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertNull(user.getPets().get(0).getPetName(), "should not inherit auto-mapping");
      Assertions.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    }
  }

  @Test
  void shouldIgnorePartialAutoMappingBehavior_InlineNestedResultMap() {
    // For nested resultMaps, PARTIAL works the same as NONE
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_Inline(2);
      Assertions.assertEquals(Integer.valueOf(2), user.getId());
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertNull(user.getPets().get(0).getPetName(), "should not inherit auto-mapping");
      Assertions.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    }
  }

  @Test
  void shouldRespectFullAutoMappingBehavior_InlineNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.FULL);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_Inline(2);
      Assertions.assertEquals(Integer.valueOf(2), user.getId());
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertEquals("Chien", user.getPets().get(0).getPetName());
      Assertions.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    }
  }

  @Test
  void shouldIgnorePartialAutoMappingBehavior_ExternalNestedResultMap() {
    // For nested resultMaps, PARTIAL works the same as NONE
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_External(2);
      Assertions.assertEquals(Integer.valueOf(2), user.getId());
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertNull(user.getPets().get(0).getPetName(), "should not inherit auto-mapping");
      Assertions.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    }
  }

  @Test
  void shouldRespectFullAutoMappingBehavior_ExternalNestedResultMap() {
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.FULL);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserWithPets_External(2);
      Assertions.assertEquals(Integer.valueOf(2), user.getId());
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertEquals("Chien", user.getPets().get(0).getPetName());
      Assertions.assertEquals("John", user.getPets().get(0).getBreeder().getBreederName());
    }
  }

  @Test
  void shouldGetBooks() {
    // set automapping to default partial
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      // no errors throw
      List<Book> books = mapper.getBooks();
      Assertions.assertTrue(!books.isEmpty(), "should return results,no errors throw");
    }
  }

  @Test
  void shouldUpdateFinalField() {
    // set automapping to default partial
    sqlSessionFactory.getConfiguration().setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Article article = mapper.getArticle();
      // Java Language Specification 17.5.3 Subsequent Modification of Final Fields
      // http://docs.oracle.com/javase/specs/jls/se5.0/html/memory.html#17.5.3
      // The final field should be updated in mapping
      Assertions.assertTrue(article.version > 0, "should update version in mapping");
    }
  }
}
