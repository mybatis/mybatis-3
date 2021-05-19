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
package org.apache.ibatis.submitted.constructor_columnprefix;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConstructorColumnPrefixTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/constructor_columnprefix/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/constructor_columnprefix/CreateDB.sql");
  }

  @Test
  void shouldGetArticles() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Article> articles = mapper.getArticles();
      assertArticles(articles);
    }
  }

  @Test
  void shouldGetArticlesAnno() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Article> articles = mapper.getArticlesAnno();
      assertArticles(articles);
    }
  }

  void assertArticles(List<Article> articles) {
    assertEquals(2, articles.size());
    Article article1 = articles.get(0);
    assertEquals(Integer.valueOf(1), article1.getId().getId());
    assertEquals("Article 1", article1.getName());
    assertEquals("Mary", article1.getAuthor().getName());
    assertEquals("Bob", article1.getCoauthor().getName());
    Article article2 = articles.get(1);
    assertEquals(Integer.valueOf(2), article2.getId().getId());
    assertEquals("Article 2", article2.getName());
    assertEquals("Jane", article2.getAuthor().getName());
    assertEquals("Mary", article2.getCoauthor().getName());
  }

}
