/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author Alexander Pozdnyakov
 */
public class PojoBindingTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setup() throws Exception {
    DataSource dataSource = BaseDataTest.createBlogDataSource();
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.setLazyLoadingEnabled(true);
    configuration.setUseActualParamName(false); // to test legacy style reference (#{0} #{1})
    configuration.addMapper(PojoBoundAuthorMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  void shouldSelectAuthorAndMapUsingAnnotations() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PojoBoundAuthorMapper mapper = session.getMapper(PojoBoundAuthorMapper.class);
      PojoBoundAuthorMapper.AnnotatedAuthor author = mapper.selectAuthor(101L);
      assertEquals(101L, author.getId());
      assertEquals("jim", author.getUsername());
      assertEquals("********", author.getPassword());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals(0, author.getBio().length());
    }
  }

  @Test
  void shouldSelectAllAuthorsAndMapUsingAnnotations() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PojoBoundAuthorMapper mapper = session.getMapper(PojoBoundAuthorMapper.class);
      List<PojoBoundAuthorMapper.AnnotatedAuthor> authors = mapper.selectAllAuthors();
      assertEquals(2, authors.size());
      assertEquals(101L, authors.get(0).getId());
      assertEquals("jim", authors.get(0).getUsername());
      assertEquals("********", authors.get(0).getPassword());
      assertEquals("jim@ibatis.apache.org", authors.get(0).getEmail());
      assertEquals(0, authors.get(0).getBio().length());
      assertEquals(102L, authors.get(1).getId());
      assertEquals("sally", authors.get(1).getUsername());
      assertEquals("********", authors.get(1).getPassword());
      assertEquals("sally@ibatis.apache.org", authors.get(1).getEmail());
      assertNull(authors.get(1).getBio());
    }
  }

  @Test
  void shouldSelectAllBlogsWithAuthorAndPostsAndMapUsingAnnotations() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      PojoBoundAuthorMapper mapper = session.getMapper(PojoBoundAuthorMapper.class);
      List<PojoBoundAuthorMapper.AnnotatedBlog> blogs = mapper.selectBlogsWithAuthorAndPosts();
      assertEquals(2, blogs.size());
      assertNotNull(blogs.get(0).getAuthor());
      assertEquals(101L, blogs.get(0).getAuthor().getId());
      assertNotNull(blogs.get(0).getPosts());
      assertEquals(2, blogs.get(0).getPosts().size());
    }
  }
}
