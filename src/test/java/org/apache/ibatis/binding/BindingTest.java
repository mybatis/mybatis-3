/**
 *    Copyright 2009-2016 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.Proxy;

import javax.sql.DataSource;

import net.sf.cglib.proxy.Factory;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.domain.blog.DraftPost;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class BindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    DataSource dataSource = BaseDataTest.createBlogDataSource();
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DDL);
    BaseDataTest.runScript(dataSource, BaseDataTest.BLOG_DATA);
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.setLazyLoadingEnabled(true);
    configuration.setUseActualParamName(false); // to test legacy style reference (#{0} #{1})
    configuration.getTypeAliasRegistry().registerAlias(Blog.class);
    configuration.getTypeAliasRegistry().registerAlias(Post.class);
    configuration.getTypeAliasRegistry().registerAlias(Author.class);
    configuration.addMapper(BoundBlogMapper.class);
    configuration.addMapper(BoundAuthorMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  public void shouldSelectBlogWithPostsUsingSubSelect() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog b = mapper.selectBlogWithPostsUsingSubSelect(1);
      assertEquals(1, b.getId());
      session.close();
      assertNotNull(b.getAuthor());
      assertEquals(101, b.getAuthor().getId());
      assertEquals("jim", b.getAuthor().getUsername());
      assertEquals("********", b.getAuthor().getPassword());
      assertEquals(2, b.getPosts().size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsInList() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      List<Post> posts = mapper.findPostsInList(new ArrayList<Integer>() {{
        add(1);
        add(3);
        add(5);
      }});
      assertEquals(3, posts.size());
      session.rollback();
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsInArray() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Integer[] params = new Integer[]{1, 3, 5};
      List<Post> posts = mapper.findPostsInArray(params);
      assertEquals(3, posts.size());
      session.rollback();
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldfindThreeSpecificPosts() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      List<Post> posts = mapper.findThreeSpecificPosts(1, new RowBounds(1, 1), 3, 5);
      assertEquals(1, posts.size());
      assertEquals(3, posts.get(0).getId());
      session.rollback();
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertAuthorWithSelectKey() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Author author = new Author(-1, "cbegin", "******", "cbegin@nowhere.com", "N/A", Section.NEWS);
      int rows = mapper.insertAuthor(author);
      assertEquals(1, rows);
      session.rollback();
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertAuthorWithSelectKeyAndDynamicParams() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Author author = new Author(-1, "cbegin", "******", "cbegin@nowhere.com", "N/A", Section.NEWS);
      int rows = mapper.insertAuthorDynamic(author);
      assertEquals(1, rows);
      assertFalse(-1 == author.getId()); // id must be autogenerated
      Author author2 = mapper.selectAuthor(author.getId());
      assertNotNull(author2);
      assertEquals(author.getEmail(), author2.getEmail());
      session.rollback();
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectRandom() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Integer x = mapper.selectRandom();
      assertNotNull(x);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectListOfBlogsStatement() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogs();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }
  
  @Test
  public void shouldExecuteBoundSelectMapOfBlogsById() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map<Integer,Blog> blogs = mapper.selectBlogsAsMapById();
      assertEquals(2, blogs.size());
      for(Map.Entry<Integer,Blog> blogEntry : blogs.entrySet()) {
        assertEquals(blogEntry.getKey(), (Integer) blogEntry.getValue().getId());
      }
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteMultipleBoundSelectOfBlogsByIdInWithProvidedResultHandlerBetweenSessions() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      final DefaultResultHandler handler = new DefaultResultHandler();
      session.select("selectBlogsAsMapById", handler);

      //new session
      session.close();
      session = sqlSessionFactory.openSession();

      final DefaultResultHandler moreHandler = new DefaultResultHandler();
      session.select("selectBlogsAsMapById", moreHandler);

      assertEquals(2, handler.getResultList().size());
      assertEquals(2, moreHandler.getResultList().size());

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteMultipleBoundSelectOfBlogsByIdInWithProvidedResultHandlerInSameSession() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      final DefaultResultHandler handler = new DefaultResultHandler();
      session.select("selectBlogsAsMapById", handler);

      final DefaultResultHandler moreHandler = new DefaultResultHandler();
      session.select("selectBlogsAsMapById", moreHandler);

      assertEquals(2, handler.getResultList().size());
      assertEquals(2, moreHandler.getResultList().size());

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteMultipleBoundSelectMapOfBlogsByIdInSameSessionWithoutClearingLocalCache() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map<Integer,Blog> blogs = mapper.selectBlogsAsMapById();
      Map<Integer,Blog> moreBlogs = mapper.selectBlogsAsMapById();
      assertEquals(2, blogs.size());
      assertEquals(2, moreBlogs.size());
      for(Map.Entry<Integer,Blog> blogEntry : blogs.entrySet()) {
        assertEquals(blogEntry.getKey(), (Integer) blogEntry.getValue().getId());
      }
      for(Map.Entry<Integer,Blog> blogEntry : moreBlogs.entrySet()) {
        assertEquals(blogEntry.getKey(), (Integer) blogEntry.getValue().getId());
      }
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteMultipleBoundSelectMapOfBlogsByIdBetweenTwoSessionsWithGlobalCacheEnabled() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map<Integer,Blog> blogs = mapper.selectBlogsAsMapById();
      session.close();

      //New Session
      session = sqlSessionFactory.openSession();
      mapper = session.getMapper(BoundBlogMapper.class);
      Map<Integer,Blog> moreBlogs = mapper.selectBlogsAsMapById();
      assertEquals(2, blogs.size());
      assertEquals(2, moreBlogs.size());
      for(Map.Entry<Integer,Blog> blogEntry : blogs.entrySet()) {
        assertEquals(blogEntry.getKey(), (Integer) blogEntry.getValue().getId());
      }
      for(Map.Entry<Integer,Blog> blogEntry : moreBlogs.entrySet()) {
        assertEquals(blogEntry.getKey(), (Integer) blogEntry.getValue().getId());
      }
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectListOfBlogsUsingXMLConfig() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogsFromXML();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectListOfBlogsStatementUsingProvider() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogsUsingProvider();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectListOfBlogsAsMaps() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Map<String,Object>> blogs = mapper.selectBlogsAsMaps();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectListOfPostsLike() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Post> posts = mapper.selectPostsLike(new RowBounds(1,1),"%a%");
      assertEquals(1, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectListOfPostsLikeTwoParameters() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Post> posts = mapper.selectPostsLikeSubjectAndBody(new RowBounds(1,1),"%a%","%a%");
      assertEquals(1, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectOneBlogStatement() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlog(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectOneBlogStatementWithConstructor() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogUsingConstructor(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
      assertNotNull("author should not be null", blog.getAuthor());
      List<Post> posts = blog.getPosts();
      assertTrue("posts should not be empty", posts != null && !posts.isEmpty());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectBlogUsingConstructorWithResultMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogUsingConstructorWithResultMap(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
      assertNotNull("author should not be null", blog.getAuthor());
      List<Post> posts = blog.getPosts();
      assertTrue("posts should not be empty", posts != null && !posts.isEmpty());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectBlogUsingConstructorWithResultMapAndProperties() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogUsingConstructorWithResultMapAndProperties(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
      assertNotNull("author should not be null", blog.getAuthor());
      Author author = blog.getAuthor();
      assertEquals(101, author.getId());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals("jim", author.getUsername());
      List<Post> posts = blog.getPosts();
      assertTrue("posts should not be empty", posts != null);
      assertEquals(2, posts.size());
    } finally {
      session.close();
    }
  }
  
  @Ignore
  @Test // issue #480 and #101
  public void shouldExecuteBoundSelectBlogUsingConstructorWithResultMapCollection() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogUsingConstructorWithResultMapCollection(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
      assertNotNull("author should not be null", blog.getAuthor());
      List<Post> posts = blog.getPosts();
      assertTrue("posts should not be empty", posts != null && !posts.isEmpty());
    } finally {
      session.close();
    }
  }
  
  @Test
  public void shouldExecuteBoundSelectOneBlogStatementWithConstructorUsingXMLConfig() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogByIdUsingConstructor(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
      assertNotNull("author should not be null", blog.getAuthor());
      List<Post> posts = blog.getPosts();
      assertTrue("posts should not be empty", posts != null && !posts.isEmpty());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneBlogAsMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map<String,Object> blog = mapper.selectBlogAsMap(new HashMap<String, Object>() {
        {
          put("id", 1);
        }
      });
      assertEquals(1, blog.get("ID"));
      assertEquals("Jim Business", blog.get("TITLE"));
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthor() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Author author = mapper.selectAuthor(101);
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());
      assertEquals("********", author.getPassword());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals("", author.getBio());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorFromCache() {
    Author author1 = selectOneAuthor();
    Author author2 = selectOneAuthor();
    assertTrue("Same (cached) instance should be returned unless rollback is called.", author1 == author2);
  }

  private Author selectOneAuthor() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      return mapper.selectAuthor(101);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorByConstructor() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Author author = mapper.selectAuthorConstructor(101);
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());
      assertEquals("********", author.getPassword());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals("", author.getBio());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectDraftTypedPosts() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Post> posts = mapper.selectPosts();
      assertEquals(5, posts.size());
      assertTrue(posts.get(0) instanceof DraftPost);
      assertFalse(posts.get(1) instanceof DraftPost);
      assertTrue(posts.get(2) instanceof DraftPost);
      assertFalse(posts.get(3) instanceof DraftPost);
      assertFalse(posts.get(4) instanceof DraftPost);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectDraftTypedPostsWithResultMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Post> posts = mapper.selectPostsWithResultMap();
      assertEquals(5, posts.size());
      assertTrue(posts.get(0) instanceof DraftPost);
      assertFalse(posts.get(1) instanceof DraftPost);
      assertTrue(posts.get(2) instanceof DraftPost);
      assertFalse(posts.get(3) instanceof DraftPost);
      assertFalse(posts.get(4) instanceof DraftPost);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldReturnANotNullToString() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      assertNotNull(mapper.toString());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldReturnANotNullHashCode() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      assertNotNull(mapper.hashCode());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldCompareTwoMappers() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      BoundBlogMapper mapper2 = session.getMapper(BoundBlogMapper.class);
      assertFalse(mapper.equals(mapper2));
    } finally {
      session.close();
    }
  }

  @Test(expected = Exception.class)
  public void shouldFailWhenSelectingOneBlogWithNonExistentParam() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      mapper.selectBlogByNonExistentParam(1);
    } finally {
      session.close();
    }
  }

  @Test(expected = Exception.class)
  public void shouldFailWhenSelectingOneBlogWithNullParam() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      mapper.selectBlogByNullParam(null);
    } finally {
      session.close();
    }
  }

  @Test // Decided that maps are dynamic so no existent params do not fail
  public void shouldFailWhenSelectingOneBlogWithNonExistentNestedParam() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      mapper.selectBlogByNonExistentNestedParam(1, Collections.<String, Object>emptyMap());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithDefault30ParamNames() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogByDefault30ParamNames(1, "Jim Business");
      assertNotNull(blog);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithDefault31ParamNames() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogByDefault31ParamNames(1, "Jim Business");
      assertNotNull(blog);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithAParamNamedValue() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlogWithAParamNamedValue("id", 1, "Jim Business");
      assertNotNull(blog);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldCacheMapperMethod() throws Exception {
    final SqlSession session = sqlSessionFactory.openSession();
    try {

      // Create another mapper instance with a method cache we can test against:
      final MapperProxyFactory<BoundBlogMapper> mapperProxyFactory = new MapperProxyFactory<BoundBlogMapper>(BoundBlogMapper.class);
      assertEquals(BoundBlogMapper.class, mapperProxyFactory.getMapperInterface());
      final BoundBlogMapper mapper = mapperProxyFactory.newInstance(session);
      assertNotSame(mapper, mapperProxyFactory.newInstance(session));
      assertTrue(mapperProxyFactory.getMethodCache().isEmpty());

      // Mapper methods we will call later:
      final Method selectBlog = BoundBlogMapper.class.getMethod("selectBlog", Integer.TYPE);
      final Method selectBlogByIdUsingConstructor = BoundBlogMapper.class.getMethod("selectBlogByIdUsingConstructor", Integer.TYPE);

      // Call mapper method and verify it is cached:
      mapper.selectBlog(1);
      assertEquals(1, mapperProxyFactory.getMethodCache().size());
      assertTrue(mapperProxyFactory.getMethodCache().containsKey(selectBlog));
      final MapperMethod cachedSelectBlog = mapperProxyFactory.getMethodCache().get(selectBlog);

      // Call mapper method again and verify the cache is unchanged:
      session.clearCache();
      mapper.selectBlog(1);
      assertEquals(1, mapperProxyFactory.getMethodCache().size());
      assertSame(cachedSelectBlog, mapperProxyFactory.getMethodCache().get(selectBlog));

      // Call another mapper method and verify that it shows up in the cache as well:
      session.clearCache();
      mapper.selectBlogByIdUsingConstructor(1);
      assertEquals(2, mapperProxyFactory.getMethodCache().size());
      assertSame(cachedSelectBlog, mapperProxyFactory.getMethodCache().get(selectBlog));
      assertTrue(mapperProxyFactory.getMethodCache().containsKey(selectBlogByIdUsingConstructor));

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldGetBlogsWithAuthorsAndPosts() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogsWithAutorAndPosts();
      assertEquals(2, blogs.size());
      assertTrue(blogs.get(0) instanceof Proxy);
      assertEquals(101, blogs.get(0).getAuthor().getId());
      assertEquals(1, blogs.get(0).getPosts().size());
      assertEquals(1, blogs.get(0).getPosts().get(0).getId());
      assertTrue(blogs.get(1) instanceof Proxy);      
      assertEquals(102, blogs.get(1).getAuthor().getId());
      assertEquals(1, blogs.get(1).getPosts().size());
      assertEquals(2, blogs.get(1).getPosts().get(0).getId());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldGetBlogsWithAuthorsAndPostsEagerly() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogsWithAutorAndPostsEagerly();
      assertEquals(2, blogs.size());
      assertFalse(blogs.get(0) instanceof Factory);
      assertEquals(101, blogs.get(0).getAuthor().getId());
      assertEquals(1, blogs.get(0).getPosts().size());
      assertEquals(1, blogs.get(0).getPosts().get(0).getId());
      assertFalse(blogs.get(1) instanceof Factory);      
      assertEquals(102, blogs.get(1).getAuthor().getId());
      assertEquals(1, blogs.get(1).getPosts().size());
      assertEquals(2, blogs.get(1).getPosts().get(0).getId());
    } finally {
      session.close();
    }
  }

  @Test
  public void executeWithResultHandlerAndRowBounds() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      final DefaultResultHandler handler = new DefaultResultHandler();
      mapper.collectRangeBlogs(handler, new RowBounds(1, 1));

      assertEquals(1, handler.getResultList().size());
      Blog blog = (Blog) handler.getResultList().get(0);
      assertEquals(2, blog.getId());

    } finally {
      session.close();
    }
  }

  @Test
  public void executeWithMapKeyAndRowBounds() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map<Integer, Blog> blogs = mapper.selectRangeBlogsAsMapById(new RowBounds(1, 1));

      assertEquals(1, blogs.size());
      Blog blog = blogs.get(2);
      assertEquals(2, blog.getId());

    } finally {
      session.close();
    }
  }

  @Test
  public void executeWithCursorAndRowBounds() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Cursor<Blog> blogs = mapper.openRangeBlogs(new RowBounds(1, 1));

      Iterator<Blog> blogIterator = blogs.iterator();
      Blog blog = blogIterator.next();
      assertEquals(2, blog.getId());
      assertFalse(blogIterator.hasNext());

    } finally {
      session.close();
    }
  }

  @Test
  public void registeredMappers() {
    Collection<Class<?>> mapperClasses = sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers();
    assertEquals(2, mapperClasses.size());
    assertTrue(mapperClasses.contains(BoundBlogMapper.class));
    assertTrue(mapperClasses.contains(BoundAuthorMapper.class));
  }

}
