/*
 *    Copyright 2009-2012 The MyBatis Team
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

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

public class NestedQueryCacheTest extends BaseDataTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested_query_cache/MapperConfig.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    createBlogDataSource();
  }

  @Test
  public void testThatNestedQueryItemsAreRetrievedFromCache() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    final Author author;
    try {
      final AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
      author = authorMapper.selectAuthor(101);
      
      // ensure that author is cached
      final Author cachedAuthor = authorMapper.selectAuthor(101);
      assertThat("cached author", author, sameInstance(cachedAuthor));
    } finally {
      sqlSession.close();
    }

    // open a new session
    sqlSession = sqlSessionFactory.openSession();
    try {
      final BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

      // ensure that nested author within blog is cached
      assertThat("blog author", blogMapper.selectBlog(1).getAuthor(), sameInstance(author));
      assertThat("blog author", blogMapper.selectBlogUsingConstructor(1).getAuthor(), sameInstance(author));
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void testThatNestedQueryItemsAreRetrievedIfNotInCache() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    Author author = null;
    try {
      final BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);
      author = blogMapper.selectBlog(1).getAuthor();
      
      // ensure that nested author within blog is cached
      assertNotNull("blog author", blogMapper.selectBlog(1).getAuthor());
      assertNotNull("blog author", blogMapper.selectBlogUsingConstructor(1).getAuthor());
    } finally {
      sqlSession.close();
    }

    // open a new session
    sqlSession = sqlSessionFactory.openSession();    
    try {
      final AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
      Author cachedAuthor = authorMapper.selectAuthor(101);

      // ensure that nested author within blog is cached
      assertThat("blog author", cachedAuthor, sameInstance(author));
      
    } finally {
      sqlSession.close();
    }
    
  }  
}