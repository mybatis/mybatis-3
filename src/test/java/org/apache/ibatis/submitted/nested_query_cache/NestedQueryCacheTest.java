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
package org.apache.ibatis.submitted.nested_query_cache;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class NestedQueryCacheTest extends BaseDataTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested_query_cache/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    createBlogDataSource();
  }

  @Test
  public void testThatNestedQueryItemsAreRetrievedFromCache() throws Exception {
    final Author author;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
      author = authorMapper.selectAuthor(101);
      
      // ensure that author is cached
      final Author cachedAuthor = authorMapper.selectAuthor(101);
      assertThat(author).isSameAs(cachedAuthor);
    }

    // open a new session
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

      // ensure that nested author within blog is cached
      assertThat(blogMapper.selectBlog(1).getAuthor()).isSameAs(author);
      assertThat(blogMapper.selectBlogUsingConstructor(1).getAuthor()).isSameAs(author);
    }
  }
  
  @Test
  public void testThatNestedQueryItemsAreRetrievedIfNotInCache() throws Exception {
    Author author;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);
      author = blogMapper.selectBlog(1).getAuthor();
      
      // ensure that nested author within blog is cached
      assertNotNull("blog author", blogMapper.selectBlog(1).getAuthor());
      assertNotNull("blog author", blogMapper.selectBlogUsingConstructor(1).getAuthor());
    }

    // open a new session
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
      Author cachedAuthor = authorMapper.selectAuthor(101);

      // ensure that nested author within blog is cached
      assertThat(cachedAuthor).isSameAs(author);
    }
    
  }  
}