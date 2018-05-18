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
package org.apache.ibatis.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.Proxy;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Blog;
import org.apache.ibatis.domain.blog.Comment;
import org.apache.ibatis.domain.blog.DraftPost;
import org.apache.ibatis.domain.blog.ImmutableAuthor;
import org.apache.ibatis.domain.blog.Post;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.domain.blog.Tag;
import org.apache.ibatis.domain.blog.mappers.AuthorMapper;
import org.apache.ibatis.domain.blog.mappers.AuthorMapperWithMultipleHandlers;
import org.apache.ibatis.domain.blog.mappers.AuthorMapperWithRowBounds;
import org.apache.ibatis.domain.blog.mappers.BlogMapper;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlSessionTest extends BaseDataTest {
  private static SqlSessionFactory sqlMapper;

  @BeforeClass
  public static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    sqlMapper = new SqlSessionFactoryBuilder().build(reader);
  }

  @Test
  public void shouldResolveBothSimpleNameAndFullyQualifiedName() {
    Configuration c = new Configuration();
    final String fullName = "com.mycache.MyCache";
    final String shortName = "MyCache";
    final PerpetualCache cache = new PerpetualCache(fullName);
    c.addCache(cache);
    assertEquals(cache, c.getCache(fullName));
    assertEquals(cache, c.getCache(shortName));
  }

  @Test(expected=IllegalArgumentException.class)
  public void shouldFailOverToMostApplicableSimpleName() {
    Configuration c = new Configuration();
    final String fullName = "com.mycache.MyCache";
    final String invalidName = "unknown.namespace.MyCache";
    final PerpetualCache cache = new PerpetualCache(fullName);
    c.addCache(cache);
    assertEquals(cache, c.getCache(fullName));
    assertEquals(cache, c.getCache(invalidName));
  }

  @Test
  public void shouldSucceedWhenFullyQualifiedButFailDueToAmbiguity() {
    Configuration c = new Configuration();

    final String name1 = "com.mycache.MyCache";
    final PerpetualCache cache1 = new PerpetualCache(name1);
    c.addCache(cache1);

    final String name2 = "com.other.MyCache";
    final PerpetualCache cache2 = new PerpetualCache(name2);
    c.addCache(cache2);

    final String shortName = "MyCache";

    assertEquals(cache1, c.getCache(name1));
    assertEquals(cache2, c.getCache(name2));

    try {
      c.getCache(shortName);
      fail("Exception expected.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("ambiguous"));
    }

  }

  @Test
  public void shouldFailToAddDueToNameConflict() {
    Configuration c = new Configuration();
    final String fullName = "com.mycache.MyCache";
    final PerpetualCache cache = new PerpetualCache(fullName);
    try {
      c.addCache(cache);
      c.addCache(cache);
      fail("Exception expected.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("already contains value"));
    }
  }

  @Test
  public void shouldOpenAndClose() {
    SqlSession session = sqlMapper.openSession(TransactionIsolationLevel.SERIALIZABLE);
    session.close();
  }

  @Test
  public void shouldCommitAnUnUsedSqlSession() {
    try (SqlSession session = sqlMapper.openSession(TransactionIsolationLevel.SERIALIZABLE)) {
      session.commit(true);
    }
  }

  @Test
  public void shouldRollbackAnUnUsedSqlSession() {
    try (SqlSession session = sqlMapper.openSession(TransactionIsolationLevel.SERIALIZABLE)) {
      session.rollback(true);
    }
  }

  @Test
  public void shouldSelectAllAuthors() {
    try (SqlSession session = sqlMapper.openSession(TransactionIsolationLevel.SERIALIZABLE)) {
      List<Author> authors = session.selectList("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAllAuthors");
      assertEquals(2, authors.size());
    }
  }

  @Test(expected=TooManyResultsException.class)
  public void shouldFailWithTooManyResultsException() {
    try (SqlSession session = sqlMapper.openSession(TransactionIsolationLevel.SERIALIZABLE)) {
      session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAllAuthors");
    }
  }
  
  @Test
  public void shouldSelectAllAuthorsAsMap() {
    try (SqlSession session = sqlMapper.openSession(TransactionIsolationLevel.SERIALIZABLE)) {
      final Map<Integer,Author> authors = session.selectMap("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAllAuthors", "id");
      assertEquals(2, authors.size());
      for(Map.Entry<Integer,Author> authorEntry : authors.entrySet()) {
        assertEquals(authorEntry.getKey(), (Integer) authorEntry.getValue().getId());
      }
    }
  }

  @Test
  public void shouldSelectCountOfPosts() {
    try (SqlSession session = sqlMapper.openSession()) {
      Integer count = session.selectOne("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectCountOfPosts");
      assertEquals(5, count.intValue());
    }
  }

  @Test
  public void shouldEnsureThatBothEarlyAndLateResolutionOfNesteDiscriminatorsResolesToUseNestedResultSetHandler() {
    try (SqlSession session = sqlMapper.openSession()) {
      Configuration configuration = sqlMapper.getConfiguration();
      assertTrue(configuration.getResultMap("org.apache.ibatis.domain.blog.mappers.BlogMapper.earlyNestedDiscriminatorPost").hasNestedResultMaps());
      assertTrue(configuration.getResultMap("org.apache.ibatis.domain.blog.mappers.BlogMapper.lateNestedDiscriminatorPost").hasNestedResultMaps());
    }
  }

  @Test
  public void shouldSelectOneAuthor() {
    try (SqlSession session = sqlMapper.openSession()) {
      Author author = session.selectOne(
          "org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", new Author(101));
      assertEquals(101, author.getId());
      assertEquals(Section.NEWS, author.getFavouriteSection());
    }
  }

  @Test
  public void shouldSelectOneAuthorAsList() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Author> authors = session.selectList(
          "org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", new Author(101));
      assertEquals(101, authors.get(0).getId());
      assertEquals(Section.NEWS, authors.get(0).getFavouriteSection());
    }
  }

  @Test
  public void shouldSelectOneImmutableAuthor() {
    try (SqlSession session = sqlMapper.openSession()) {
      ImmutableAuthor author = session.selectOne(
          "org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectImmutableAuthor", new Author(101));
      assertEquals(101, author.getId());
      assertEquals(Section.NEWS, author.getFavouriteSection());
    }
  }

  @Test
  public void shouldSelectOneAuthorWithInlineParams() {
    try (SqlSession session = sqlMapper.openSession()) {
      Author author = session.selectOne(
          "org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthorWithInlineParams", new Author(101));
      assertEquals(101, author.getId());
    }
  }

  @Test
  public void shouldInsertAuthor() {
    try (SqlSession session = sqlMapper.openSession()) {
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      int updates = session.insert("org.apache.ibatis.domain.blog.mappers.AuthorMapper.insertAuthor", expected);
      assertEquals(1, updates);
      Author actual = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", new Author(500));
      assertNotNull(actual);
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getUsername(), actual.getUsername());
      assertEquals(expected.getPassword(), actual.getPassword());
      assertEquals(expected.getEmail(), actual.getEmail());
      assertEquals(expected.getBio(), actual.getBio());
    }
  }

  @Test
  public void shouldUpdateAuthorImplicitRollback() {
    try (SqlSession session = sqlMapper.openSession()) {
      Author original = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      int updates = session.update("org.apache.ibatis.domain.blog.mappers.AuthorMapper.updateAuthor", original);
      assertEquals(1, updates);
      Author updated = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
    }
    try (SqlSession session = sqlMapper.openSession()) {
      Author updated = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals("jim@ibatis.apache.org", updated.getEmail());
    }
  }

  @Test
  public void shouldUpdateAuthorCommit() {
    Author original;
    try (SqlSession session = sqlMapper.openSession()) {
      original = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      int updates = session.update("org.apache.ibatis.domain.blog.mappers.AuthorMapper.updateAuthor", original);
      assertEquals(1, updates);
      Author updated = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
      session.commit();
    }
    try (SqlSession session = sqlMapper.openSession()) {
      Author updated = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
    }
  }

  @Test
  public void shouldUpdateAuthorIfNecessary() {
    Author original;
    try (SqlSession session = sqlMapper.openSession()) {
      original = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      original.setBio(null);
      int updates = session.update("org.apache.ibatis.domain.blog.mappers.AuthorMapper.updateAuthorIfNecessary", original);
      assertEquals(1, updates);
      Author updated = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
      session.commit();
    }
    try (SqlSession session = sqlMapper.openSession()) {
      Author updated = session.selectOne("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
    }
  }

  @Test
  public void shouldDeleteAuthor() {
    try (SqlSession session = sqlMapper.openSession()) {
      final int id = 102;

      List<Author> authors = session.selectList("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", id);
      assertEquals(1, authors.size());

      int updates = session.delete("org.apache.ibatis.domain.blog.mappers.AuthorMapper.deleteAuthor", id);
      assertEquals(1, updates);

      authors = session.selectList("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", id);
      assertEquals(0, authors.size());

      session.rollback();
      authors = session.selectList("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAuthor", id);
      assertEquals(1, authors.size());
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingSubSelects() {
    try (SqlSession session = sqlMapper.openSession()) {
      Blog blog = session.selectOne("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect", 1);
      assertEquals("Jim Business", blog.getTitle());
      assertEquals(2, blog.getPosts().size());
      assertEquals("Corn nuts", blog.getPosts().get(0).getSubject());
      assertEquals(101, blog.getAuthor().getId());
      assertEquals("jim", blog.getAuthor().getUsername());
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingSubSelectsLazily() {
    try (SqlSession session = sqlMapper.openSession()) {
      Blog blog = session.selectOne("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelectLazily", 1);
      Assert.assertTrue(blog instanceof Proxy);
      assertEquals("Jim Business", blog.getTitle());
      assertEquals(2, blog.getPosts().size());
      assertEquals("Corn nuts", blog.getPosts().get(0).getSubject());
      assertEquals(101, blog.getAuthor().getId());
      assertEquals("jim", blog.getAuthor().getUsername());
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingJoin() {
    try (SqlSession session = sqlMapper.openSession()) {
      Blog blog = session.selectOne("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
      assertEquals("Jim Business", blog.getTitle());

      final Author author = blog.getAuthor();
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());

      final List<Post> posts = blog.getPosts();
      assertEquals(2, posts.size());

      final Post post = blog.getPosts().get(0);
      assertEquals(1, post.getId());
      assertEquals("Corn nuts", post.getSubject());

      final List<Comment> comments = post.getComments();
      assertEquals(2, comments.size());

      final List<Tag> tags = post.getTags();
      assertEquals(3, tags.size());

      final Comment comment = comments.get(0);
      assertEquals(1, comment.getId());

      assertEquals(DraftPost.class, blog.getPosts().get(0).getClass());
      assertEquals(Post.class, blog.getPosts().get(1).getClass());
    }
  }

  @Test
  public void shouldSelectNestedBlogWithPostsAndAuthorUsingJoin() {
    try (SqlSession session = sqlMapper.openSession()) {
      Blog blog = session.selectOne("org.apache.ibatis.domain.blog.mappers.NestedBlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
      assertEquals("Jim Business", blog.getTitle());

      final Author author = blog.getAuthor();
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());

      final List<Post> posts = blog.getPosts();
      assertEquals(2, posts.size());

      final Post post = blog.getPosts().get(0);
      assertEquals(1, post.getId());
      assertEquals("Corn nuts", post.getSubject());

      final List<Comment> comments = post.getComments();
      assertEquals(2, comments.size());

      final List<Tag> tags = post.getTags();
      assertEquals(3, tags.size());

      final Comment comment = comments.get(0);
      assertEquals(1, comment.getId());

      assertEquals(DraftPost.class, blog.getPosts().get(0).getClass());
      assertEquals(Post.class, blog.getPosts().get(1).getClass());
    }
  }

  @Test
  public void shouldThrowExceptionIfMappedStatementDoesNotExist() {
    try (SqlSession session = sqlMapper.openSession()) {
      session.selectList("ThisStatementDoesNotExist");
      fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    }
  }

  @Test
  public void shouldThrowExceptionIfTryingToAddStatementWithSameName() {
    Configuration config = sqlMapper.getConfiguration();
    try {
      config.addMappedStatement(config.getMappedStatement("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect"));
      fail("Expected exception to be thrown due to statement that already exists.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("already contains value for org.apache.ibatis.domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect"));
    }
  }

  @Test
  public void shouldCacheAllAuthors() {
    int first;
    try (SqlSession session = sqlMapper.openSession()) {
      List<Author> authors = session.selectList("org.apache.ibatis.builder.CachedAuthorMapper.selectAllAuthors");
      first = System.identityHashCode(authors);
      session.commit(); // commit should not be required for read/only activity.
    }
    int second;
    try (SqlSession session = sqlMapper.openSession()) {
      List<Author> authors = session.selectList("org.apache.ibatis.builder.CachedAuthorMapper.selectAllAuthors");
      second = System.identityHashCode(authors);
    }
    assertEquals(first, second);
  }

  @Test
  public void shouldNotCacheAllAuthors() throws Exception {
    int first;
    try (SqlSession session = sqlMapper.openSession()) {
      List<Author> authors = session.selectList("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAllAuthors");
      first = System.identityHashCode(authors);
    }
    int second;
    try (SqlSession session = sqlMapper.openSession()) {
      List<Author> authors = session.selectList("org.apache.ibatis.domain.blog.mappers.AuthorMapper.selectAllAuthors");
      second = System.identityHashCode(authors);
    }
    assertTrue(first != second);
  }

  @Test
  public void shouldSelectAuthorsUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      List<Author> authors = mapper.selectAllAuthors();
      assertEquals(2, authors.size());
    }
  }

  @Test
  public void shouldExecuteSelectOneAuthorUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author author = mapper.selectAuthor(101);
      assertEquals(101, author.getId());
    }
  }


  @Test
  public void shouldExecuteSelectOneAuthorUsingMapperClassThatReturnsALinedHashMap() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      LinkedHashMap<String, Object> author = mapper.selectAuthorLinkedHashMap(101);
      assertEquals(101, author.get("ID"));
    }
  }
  
  @Test
  public void shouldExecuteSelectAllAuthorsUsingMapperClassThatReturnsSet() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Collection<Author> authors = mapper.selectAllAuthorsSet();
      assertEquals(2, authors.size());
    }
  }

  @Test
  public void shouldExecuteSelectAllAuthorsUsingMapperClassThatReturnsVector() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Collection<Author> authors = mapper.selectAllAuthorsVector();
      assertEquals(2, authors.size());
    }
  }

  @Test
  public void shouldExecuteSelectAllAuthorsUsingMapperClassThatReturnsLinkedList() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Collection<Author> authors = mapper.selectAllAuthorsLinkedList();
      assertEquals(2, authors.size());
    }
  }

  @Test
  public void shouldExecuteSelectAllAuthorsUsingMapperClassThatReturnsAnArray() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author[] authors = mapper.selectAllAuthorsArray();
      assertEquals(2, authors.length);
    }
  }

  @Test
  public void shouldExecuteSelectOneAuthorUsingMapperClassWithResultHandler() {
    try (SqlSession session = sqlMapper.openSession()) {
      DefaultResultHandler handler = new DefaultResultHandler();
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      mapper.selectAuthor(101, handler);
      Author author = (Author) handler.getResultList().get(0);
      assertEquals(101, author.getId());
    }
  }

  @Test(expected=BindingException.class)
  public void shouldFailExecutingAnAnnotatedMapperClassWithResultHandler() {
    try (SqlSession session = sqlMapper.openSession()) {
      DefaultResultHandler handler = new DefaultResultHandler();
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      mapper.selectAuthor2(101, handler);
      Author author = (Author) handler.getResultList().get(0);
      assertEquals(101, author.getId());
    }
  }
  
  @Test
  public void shouldSelectAuthorsUsingMapperClassWithResultHandler() {
    try (SqlSession session = sqlMapper.openSession()) {
      DefaultResultHandler handler = new DefaultResultHandler();
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      mapper.selectAllAuthors(handler);
      assertEquals(2, handler.getResultList().size());
    }
  }

  @Test(expected = BindingException.class)
  public void shouldFailSelectOneAuthorUsingMapperClassWithTwoResultHandlers() {
    Configuration configuration = new Configuration(sqlMapper.getConfiguration().getEnvironment());
    configuration.addMapper(AuthorMapperWithMultipleHandlers.class);
    SqlSessionFactory sqlMapperWithMultipleHandlers = new DefaultSqlSessionFactory(configuration);
    try (SqlSession sqlSession = sqlMapperWithMultipleHandlers.openSession();) {
      DefaultResultHandler handler1 = new DefaultResultHandler();
      DefaultResultHandler handler2 = new DefaultResultHandler();
      AuthorMapperWithMultipleHandlers mapper = sqlSession.getMapper(AuthorMapperWithMultipleHandlers.class);
      mapper.selectAuthor(101, handler1, handler2);
    }
  }

  @Test(expected = BindingException.class)
  public void shouldFailSelectOneAuthorUsingMapperClassWithTwoRowBounds() {
    Configuration configuration = new Configuration(sqlMapper.getConfiguration().getEnvironment());
    configuration.addMapper(AuthorMapperWithRowBounds.class);
    SqlSessionFactory sqlMapperWithMultipleHandlers = new DefaultSqlSessionFactory(configuration);
    try (SqlSession sqlSession = sqlMapperWithMultipleHandlers.openSession();) {
      RowBounds bounds1 = new RowBounds(0, 1);
      RowBounds bounds2 = new RowBounds(0, 1);
      AuthorMapperWithRowBounds mapper = sqlSession.getMapper(AuthorMapperWithRowBounds.class);
      mapper.selectAuthor(101, bounds1, bounds2);
    }
  }

  @Test
  public void shouldInsertAuthorUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      Author actual = mapper.selectAuthor(500);
      assertNotNull(actual);
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getUsername(), actual.getUsername());
      assertEquals(expected.getPassword(), actual.getPassword());
      assertEquals(expected.getEmail(), actual.getEmail());
      assertEquals(expected.getBio(), actual.getBio());
    }
  }

  @Test
  public void shouldDeleteAuthorUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      int count = mapper.deleteAuthor(101);
      assertEquals(1, count);
      assertNull(mapper.selectAuthor(101));
    }
  }

  @Test
  public void shouldUpdateAuthorUsingMapperClass() throws Exception {
    try (SqlSession session = sqlMapper.openSession()) {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author expected = mapper.selectAuthor(101);
      expected.setUsername("NewUsername");
      int count = mapper.updateAuthor(expected);
      assertEquals(1, count);
      Author actual = mapper.selectAuthor(101);
      assertEquals(expected.getUsername(), actual.getUsername());
    }
  }

  @Test
  public void shouldSelectAllPostsUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      List<Map> posts = mapper.selectAllPosts();
      assertEquals(5, posts.size());
    }
  }

  @Test
  public void shouldLimitResultsUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      List<Map> posts = mapper.selectAllPosts(new RowBounds(0, 2), null);
      assertEquals(2, posts.size());
      assertEquals(1, posts.get(0).get("ID"));
      assertEquals(2, posts.get(1).get("ID"));
    }
  }

  private static class TestResultHandler implements ResultHandler {
    int count = 0;
    @Override
    public void handleResult(ResultContext context) {
      count++;
    }
  }

  @Test
  public void shouldHandleZeroParameters() {
    try (SqlSession session = sqlMapper.openSession()) {
      final TestResultHandler resultHandler = new TestResultHandler();
      session.select("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectAllPosts", resultHandler);
      assertEquals(5, resultHandler.count);
    }
  }

  private static class TestResultStopHandler implements ResultHandler {
    int count = 0;
    @Override
    public void handleResult(ResultContext context) {
      count++;
      if (count == 2) context.stop();
    }
  }

  @Test
  public void shouldStopResultHandler() {
    try (SqlSession session = sqlMapper.openSession()) {
      final TestResultStopHandler resultHandler = new TestResultStopHandler();
      session.select("org.apache.ibatis.domain.blog.mappers.BlogMapper.selectAllPosts", null, resultHandler);
      assertEquals(2, resultHandler.count);
    }
  }

  @Test
  public void shouldOffsetAndLimitResultsUsingMapperClass() {
    try (SqlSession session = sqlMapper.openSession()) {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      List<Map> posts = mapper.selectAllPosts(new RowBounds(2, 3));
      assertEquals(3, posts.size());
      assertEquals(3, posts.get(0).get("ID"));
      assertEquals(4, posts.get(1).get("ID"));
      assertEquals(5, posts.get(2).get("ID"));
    }
  }

  @Test
  public void shouldFindPostsAllPostsWithDynamicSql() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.findPost");
      assertEquals(5, posts.size());
    }
  }

  @Test
  public void shouldFindPostByIDWithDynamicSql() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.findPost",
          new HashMap<String, Integer>() {{
            put("id", 1);
          }});
      assertEquals(1, posts.size());
    }
  }

  @Test
  public void shouldFindPostsInSetOfIDsWithDynamicSql() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.findPost",
          new HashMap<String, List<Integer>>() {{
            put("ids", new ArrayList<Integer>() {{
              add(1);
              add(2);
              add(3);
            }});
          }});
      assertEquals(3, posts.size());
    }
  }

  @Test
  public void shouldFindPostsWithBlogIdUsingDynamicSql() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.findPost",
          new HashMap<String, Integer>() {{
            put("blog_id", 1);
          }});
      assertEquals(2, posts.size());
    }
  }

  @Test
  public void shouldFindPostsWithAuthorIdUsingDynamicSql() throws Exception {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.findPost",
          new HashMap<String, Integer>() {{
            put("author_id", 101);
          }});
      assertEquals(3, posts.size());
    }
  }

  @Test
  public void shouldFindPostsWithAuthorAndBlogIdUsingDynamicSql() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.findPost",
          new HashMap<String, Object>() {{
            put("ids", new ArrayList<Integer>() {{
              add(1);
              add(2);
              add(3);
            }});
            put("blog_id", 1);
          }});
      assertEquals(2, posts.size());
    }
  }

  @Test
  public void shouldFindPostsInList() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.selectPostIn",
          new ArrayList<Integer>() {{
            add(1);
            add(3);
            add(5);
          }});
      assertEquals(3, posts.size());
    }
  }

  @Test
  public void shouldFindOddPostsInList() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.selectOddPostsIn",
          new ArrayList<Integer>() {{
            add(0);
            add(1);
            add(2);
            add(3);
            add(4);
          }});
      // we're getting odd indexes, not odd values, 0 is not odd
      assertEquals(2, posts.size());
      assertEquals(1, posts.get(0).getId());
      assertEquals(3, posts.get(1).getId());
    }
  }


  @Test
  public void shouldSelectOddPostsInKeysList() {
    try (SqlSession session = sqlMapper.openSession()) {
      List<Post> posts = session.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.selectOddPostsInKeysList",
          new HashMap<String, List<Integer>>() {{put("keys",new ArrayList<Integer>() {{
            add(0);
            add(1);
            add(2);
            add(3);
            add(4);
          }});
          }});
      // we're getting odd indexes, not odd values, 0 is not odd
      assertEquals(2, posts.size());
      assertEquals(1, posts.get(0).getId());
      assertEquals(3, posts.get(1).getId());
    }
  }

}
