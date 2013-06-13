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
package org.apache.ibatis.session;

import domain.blog.*;
import domain.blog.mappers.AuthorMapper;
import domain.blog.mappers.BlogMapper;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class SqlSessionManagerTest extends BaseDataTest {

  private static SqlSessionManager manager;

  @Before
  public void initializeDB() throws Exception {
    createBlogDataSource();
  }

  @BeforeClass
  public static void setup() throws Exception {
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    manager = SqlSessionManager.newInstance(reader);
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
  public void shouldSelectAllAuthors() throws Exception {
    List<Author> authors = manager.selectList("domain.blog.mappers.AuthorMapper.selectAllAuthors");
    assertEquals(2, authors.size());
  }

  @Test
  public void shouldSelectAllComplexAuthors() throws Exception {
    List<ComplexImmutableAuthor> authors = manager.selectList("domain.blog.mappers.AuthorMapper.selectComplexAuthors");
    assertEquals(2, authors.size());
  }

  @Test
  public void shouldSelectCountOfPosts() throws Exception {
    Integer count = manager.selectOne("domain.blog.mappers.BlogMapper.selectCountOfPosts");
    assertEquals(5, count.intValue());
  }

  @Test
  public void shouldEnsureThatBothEarlyAndLateResolutionOfNesteDiscriminatorsResolesToUseNestedResultSetHandler() throws Exception {
    Configuration configuration = manager.getConfiguration();
    assertTrue(configuration.getResultMap("domain.blog.mappers.BlogMapper.earlyNestedDiscriminatorPost").hasNestedResultMaps());
    assertTrue(configuration.getResultMap("domain.blog.mappers.BlogMapper.lateNestedDiscriminatorPost").hasNestedResultMaps());
  }

  @Test
  public void shouldSelectOneAuthor() throws Exception {
    Author author = manager.selectOne(
        "domain.blog.mappers.AuthorMapper.selectAuthor", new Author(101));
    assertEquals(101, author.getId());
    assertEquals(Section.NEWS, author.getFavouriteSection());
  }

  @Test
  public void shouldSelectOneAuthorAsList() throws Exception {
    List<Author> authors = manager.selectList(
        "domain.blog.mappers.AuthorMapper.selectAuthor", new Author(101));
    assertEquals(101, authors.get(0).getId());
    assertEquals(Section.NEWS, authors.get(0).getFavouriteSection());
  }

  @Test
  public void shouldSelectOneImmutableAuthor() throws Exception {
    ImmutableAuthor author = manager.selectOne(
        "domain.blog.mappers.AuthorMapper.selectImmutableAuthor", new Author(101));
    assertEquals(101, author.getId());
    assertEquals(Section.NEWS, author.getFavouriteSection());
  }

  @Test
  public void shouldSelectOneAuthorWithInlineParams() throws Exception {
    Author author = manager.selectOne(
        "domain.blog.mappers.AuthorMapper.selectAuthorWithInlineParams", new Author(101));
    assertEquals(101, author.getId());
  }

  @Test
  public void shouldInsertAuthor() throws Exception {
    Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
    manager.insert("domain.blog.mappers.AuthorMapper.insertAuthor", expected);
    Author actual = manager.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", new Author(500));
    assertNotNull(actual);
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getUsername(), actual.getUsername());
    assertEquals(expected.getPassword(), actual.getPassword());
    assertEquals(expected.getEmail(), actual.getEmail());
    assertEquals(expected.getBio(), actual.getBio());
  }

  @Test
  public void shouldDeleteAuthor() throws Exception {
    final int id = 102;

    List<Author> authors = manager.selectList("domain.blog.mappers.AuthorMapper.selectAuthor", id);
    assertEquals(1, authors.size());

    manager.delete("domain.blog.mappers.AuthorMapper.deleteAuthor", id);
    authors = manager.selectList("domain.blog.mappers.AuthorMapper.selectAuthor", id);
    assertEquals(0, authors.size());


  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingSubSelects() throws Exception {
    Blog blog = manager.selectOne("domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect", 1);
    assertEquals("Jim Business", blog.getTitle());
    assertEquals(2, blog.getPosts().size());
    assertEquals("Corn nuts", blog.getPosts().get(0).getSubject());
    assertEquals(101, blog.getAuthor().getId());
    assertEquals("jim", blog.getAuthor().getUsername());
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingJoin() throws Exception {
    Blog blog = manager.selectOne("domain.blog.mappers.BlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
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

  @Test
  public void shouldSelectNestedBlogWithPostsAndAuthorUsingJoin() throws Exception {
    Blog blog = manager.selectOne("domain.blog.mappers.NestedBlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
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

  @Test
  public void shouldThrowExceptionIfMappedStatementDoesNotExist() throws Exception {
    try {
      manager.selectList("ThisStatementDoesNotExist");
      fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (PersistenceException e) {
      assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    }
  }

  @Test
  public void shouldThrowExceptionIfMappedStatementDoesNotExistAndSqlSessionIsOpen() throws Exception {
    try {
      manager.startManagedSession();
      manager.selectList("ThisStatementDoesNotExist");
      fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (PersistenceException e) {
      assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    } finally {
      manager.close();
    }
  }

  @Test
  public void shouldThrowExceptionIfTryingToAddStatementWithSameName() throws Exception {
    Configuration config = manager.getConfiguration();
    try {
      config.addMappedStatement(config.getMappedStatement("domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect"));
      fail("Expected exception to be thrown due to statement that already exists.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("already contains value for domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect"));
    }
  }

  @Test
  public void shouldCacheAllAuthors() throws Exception {
    int first = -1;
    int second = -1;
    List<Author> authors = manager.selectList("com.domain.CachedAuthorMapper.selectAllAuthors");
    first = System.identityHashCode(authors);

    authors = manager.selectList("com.domain.CachedAuthorMapper.selectAllAuthors");
    second = System.identityHashCode(authors);
    assertEquals(first, second);
  }

  @Test
  public void shouldNotCacheAllAuthors() throws Exception {
    int first = -1;
    int second = -1;
    List<Author> authors = manager.selectList("domain.blog.mappers.AuthorMapper.selectAllAuthors");
    first = System.identityHashCode(authors);
    authors = manager.selectList("domain.blog.mappers.AuthorMapper.selectAllAuthors");
    second = System.identityHashCode(authors);
    assertTrue(first != second);
  }

  @Test
  public void shouldSelectAuthorsUsingMapperClass() {
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    List<Author> authors = mapper.selectAllAuthors();
    assertEquals(2, authors.size());
  }

  @Test
  public void shouldExecuteSelectOneAuthorUsingMapperClass() {
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    Author author = mapper.selectAuthor(101);
    assertEquals(101, author.getId());
  }

  @Test
  public void shouldInsertAuthorUsingMapperClass() throws Exception {
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
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

  @Test
  public void shouldCommitInsertedAuthor() throws Exception {
    try {
      manager.startManagedSession();
      AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      manager.commit();
      Author actual = mapper.selectAuthor(500);
      assertNotNull(actual);
    } finally {
      manager.close();
    }
  }

  @Test
  public void shouldRollbackInsertedAuthor() throws Exception {
    try {
      manager.startManagedSession();
      AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      manager.rollback();
      Author actual = mapper.selectAuthor(500);
      assertNull(actual);
    } finally {
      manager.close();
    }
  }

  @Test
  public void shouldImplicitlyRollbackInsertedAuthor() throws Exception {
    manager.startManagedSession();
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
    mapper.insertAuthor(expected);
    manager.close();
    Author actual = mapper.selectAuthor(500);
    assertNull(actual);
  }

  @Test
  public void shouldDeleteAuthorUsingMapperClass() throws Exception {
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    int count = mapper.deleteAuthor(101);
    assertEquals(1, count);
    assertNull(mapper.selectAuthor(101));
  }

  @Test
  public void shouldUpdateAuthorUsingMapperClass() throws Exception {
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    Author expected = mapper.selectAuthor(101);
    expected.setUsername("NewUsername");
    int count = mapper.updateAuthor(expected);
    assertEquals(1, count);
    Author actual = mapper.selectAuthor(101);
    assertEquals(expected.getUsername(), actual.getUsername());
  }

  @Test
  public void shouldSelectAllPostsUsingMapperClass() throws Exception {
    BlogMapper mapper = manager.getMapper(BlogMapper.class);
    List<Map> posts = mapper.selectAllPosts();
    assertEquals(5, posts.size());
  }

  @Test
  public void shouldLimitResultsUsingMapperClass() throws Exception {
    BlogMapper mapper = manager.getMapper(BlogMapper.class);
    List<Map> posts = mapper.selectAllPosts(new RowBounds(0, 2), null);
    assertEquals(2, posts.size());
    assertEquals(1, posts.get(0).get("ID"));
    assertEquals(2, posts.get(1).get("ID"));
  }

  private static class TestResultHandler implements ResultHandler {
    int count = 0;

    public void handleResult(ResultContext context) {
      count++;
    }
  }

  @Test
  public void shouldHandleZeroParameters() throws Exception {
    final TestResultHandler resultHandler = new TestResultHandler();
    manager.select("domain.blog.mappers.BlogMapper.selectAllPosts", resultHandler);
    assertEquals(5, resultHandler.count);
  }

  private static class TestResultStopHandler implements ResultHandler {
    int count = 0;

    public void handleResult(ResultContext context) {
      count++;
      if (count == 2) context.stop();
    }
  }

  @Test
  public void shouldStopResultHandler() throws Exception {
    final TestResultStopHandler resultHandler = new TestResultStopHandler();
    manager.select("domain.blog.mappers.BlogMapper.selectAllPosts", null, resultHandler);
    assertEquals(2, resultHandler.count);
  }

  @Test
  public void shouldOffsetAndLimitResultsUsingMapperClass() throws Exception {
    BlogMapper mapper = manager.getMapper(BlogMapper.class);
    List<Map> posts = mapper.selectAllPosts(new RowBounds(2, 3));
    assertEquals(3, posts.size());
    assertEquals(3, posts.get(0).get("ID"));
    assertEquals(4, posts.get(1).get("ID"));
    assertEquals(5, posts.get(2).get("ID"));
  }

  @Test
  public void shouldFindPostsAllPostsWithDynamicSql() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.findPost");
    assertEquals(5, posts.size());
  }

  @Test
  public void shouldFindAllPostLites() throws Exception {
    List<PostLite> posts = manager.selectList("domain.blog.mappers.PostMapper.selectPostLite");
    assertEquals(4, posts.size());
  }

  @Ignore // see issue #48 (gh)
  @Test
  public void shouldFindAllPostLitesWithNestedSelect() throws Exception {
    final BlogLite blog = manager.selectOne("domain.blog.mappers.PostMapper.selectPostLite2NestedWithSelect");
    assertNotNull(blog);
    assertEquals(4, blog.getPosts().size());
  }

  @Test
  public void shouldFindAllPostLitesWithNestedResultMap() throws Exception {
    final BlogLite blog = manager.selectOne("domain.blog.mappers.PostMapper.selectPostLite2NestedWithoutSelect");
    assertNotNull(blog);
    assertEquals(4, blog.getPosts().size());
  }

  @Test
  public void shouldFindAllMutablePostLites() throws Exception {
    List<PostLite> posts = manager.selectList("domain.blog.mappers.PostMapper.selectMutablePostLite");
    assertEquals(4, posts.size());
  }

  @Test
  public void shouldFindPostByIDWithDynamicSql() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.findPost",
        new HashMap<String,Integer>() {{
          put("id", 1);
        }});
    assertEquals(1, posts.size());
  }

  @Test
  public void shouldFindPostsInSetOfIDsWithDynamicSql() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.findPost",
        new HashMap<String,List<Integer>>() {{
          put("ids", new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
          }});
        }});
    assertEquals(3, posts.size());
  }

  @Test
  public void shouldFindPostsWithBlogIdUsingDynamicSql() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.findPost",
        new HashMap<String,Integer>() {{
          put("blog_id", 1);
        }});
    assertEquals(2, posts.size());
  }

  @Test
  public void shouldFindPostsWithAuthorIdUsingDynamicSql() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.findPost",
        new HashMap<String,Integer>() {{
          put("author_id", 101);
        }});
    assertEquals(3, posts.size());
  }

  @Test
  public void shouldFindPostsWithAuthorAndBlogIdUsingDynamicSql() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.findPost",
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

  @Test
  public void shouldFindPostsInList() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.selectPostIn",
        new ArrayList<Integer>() {{
          add(1);
          add(3);
          add(5);
        }});
    assertEquals(3, posts.size());
  }

  @Test
  public void shouldFindOddPostsInList() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.selectOddPostsIn",
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


  @Test
  public void shouldSelectOddPostsInKeysList() throws Exception {
    List<Post> posts = manager.selectList("domain.blog.mappers.PostMapper.selectOddPostsInKeysList",
        new HashMap<String,List<Integer>>() {{
          put("keys", new ArrayList<Integer>() {{
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
