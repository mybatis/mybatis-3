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
package org.apache.ibatis.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.cglib.proxy.Factory;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.BeforeClass;
import org.junit.Test;

import domain.blog.Author;
import domain.blog.Blog;
import domain.blog.Post;
import domain.blog.Section;

public class BaseExecutorTest extends BaseDataTest {
  protected final Configuration config;
  protected static DataSource ds;

  @BeforeClass
  public static void setup() throws Exception {
    ds = createBlogDataSource();
  }

  public BaseExecutorTest() {
    config = new Configuration();
    config.setLazyLoadingEnabled(true);
    config.setUseGeneratedKeys(false);
    config.setMultipleResultSetsEnabled(true);
    config.setUseColumnLabel(true);
    config.setDefaultStatementTimeout(5000);
    config.setDefaultFetchSize(100);
  }

  @Test
  public void shouldInsertNewAuthorWithBeforeAutoKey() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(-1, "someone", "******", "someone@apache.org", null, Section.NEWS);
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatementWithBeforeAutoKey(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(insertStatement, author);
      assertTrue(rows > 0 || rows == BatchExecutor.BATCH_UPDATE_RETURN_VALUE);
      if (rows == BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        executor.flushStatements();
      }
      assertEquals(123456, author.getId());
      if (author.getId() != BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        List<Author> authors = executor.query(selectStatement, author.getId(), RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        executor.rollback(true);
        assertEquals(1, authors.size());
        assertEquals(author.toString(), authors.get(0).toString());
        assertTrue(author.getId() >= 10000);
      }
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldInsertNewAuthor() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(99, "someone", "******", "someone@apache.org", null, Section.NEWS);
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatement(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(insertStatement, author);
      List<Author> authors = executor.query(selectStatement, 99, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(1, authors.size());
      assertEquals(author.toString(), authors.get(0).toString());
      assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldSelectAllAuthorsAutoMapped() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectAllAuthorsAutoMappedStatement(config);
      List<Author> authors = executor.query(selectStatement, null, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      assertEquals(2, authors.size());
      Author author = authors.get(0);
      // id,username, password, email, bio, favourite_section
      // (101,'jim','********','jim@ibatis.apache.org','','NEWS');
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals("", author.getBio());
      assertEquals(Section.NEWS, author.getFavouriteSection());
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldInsertNewAuthorWithAutoKey() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(-1, "someone", "******", "someone@apache.org", null, Section.NEWS);
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatementWithAutoKey(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(insertStatement, author);
      assertTrue(rows > 0 || rows == BatchExecutor.BATCH_UPDATE_RETURN_VALUE);
      if (rows == BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        executor.flushStatements();
      }
      assertTrue(-1 != author.getId());
      if (author.getId() != BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        List<Author> authors = executor.query(selectStatement, author.getId(), RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        executor.rollback(true);
        assertEquals(1, authors.size());
        assertEquals(author.toString(), authors.get(0).toString());
        assertTrue(author.getId() >= 10000);
      }
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldInsertNewAuthorByProc() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(97, "someone", "******", "someone@apache.org", null, null);
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorProc(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(insertStatement, author);
      List<Author> authors = executor.query(selectStatement, 97, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(1, authors.size());
      assertEquals(author.toString(), authors.get(0).toString());
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldInsertNewAuthorUsingSimpleNonPreparedStatements() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(99, "someone", "******", "someone@apache.org", null, null);
      MappedStatement insertStatement = ExecutorTestHelper.createInsertAuthorWithIDof99MappedStatement(config);
      MappedStatement selectStatement = ExecutorTestHelper.createSelectAuthorWithIDof99MappedStatement(config);
      int rows = executor.update(insertStatement, null);
      List<Author> authors = executor.query(selectStatement, 99, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(1, authors.size());
      assertEquals(author.toString(), authors.get(0).toString());
      assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldUpdateAuthor() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(101, "someone", "******", "someone@apache.org", null, Section.NEWS);
      MappedStatement updateStatement = ExecutorTestHelper.prepareUpdateAuthorMappedStatement(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(updateStatement, author);
      List<Author> authors = executor.query(selectStatement, 101, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(1, authors.size());
      assertEquals(author.toString(), authors.get(0).toString());
      assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldDeleteAuthor() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      Author author = new Author(101, null, null, null, null, null);
      MappedStatement deleteStatement = ExecutorTestHelper.prepareDeleteAuthorMappedStatement(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int rows = executor.update(deleteStatement, author);
      List<Author> authors = executor.query(selectStatement, 101, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(0, authors.size());
      assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldSelectDiscriminatedPost() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectDiscriminatedPost(config);
      List<Map<String,String>> products = executor.query(selectStatement, null, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      assertEquals(5, products.size());
      for (Map<String,String> m : products) {
        if ("IMAGES".equals(m.get("SECTION"))) {
          assertNull(m.get("subject"));
        } else {
          assertNotNull(m.get("subject"));
        }
      }
    } finally {
      executor.close(false);
    }
  }

  @Test
  public void shouldSelect2DiscriminatedPosts() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectDiscriminatedPost(config);
      List<Map<String,String>> products = executor.query(selectStatement, null, new RowBounds(2, 2), Executor.NO_RESULT_HANDLER);
      assertEquals(2, products.size());
      for (Map<String,String> m : products) {
        if ("IMAGES".equals(m.get("SECTION"))) {
          assertNull(m.get("subject"));
        } else {
          assertNotNull(m.get("subject"));
        }
      }
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldSelectTwoSetsOfAuthorsViaProc() throws Exception {
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectTwoSetsOfAuthorsProc(config);
      List<List<Author>> authorSets = executor.query(selectStatement, new HashMap<String, Object>() {
        {
          put("id1", 101);
          put("id2", 102);
        }
      }, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      assertEquals(2, authorSets.size());
      for (List<Author> authors : authorSets) {
        assertEquals(2, authors.size());
        for (Object author : authors) {
          assertTrue(author instanceof Author);
        }
      }
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test 
  public void shouldSelectAuthorViaOutParams() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectAuthorViaOutParams(config);
      Author author = new Author(102, null, null, null, null, null);
      executor.query(selectStatement, author, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      assertEquals("sally", author.getUsername());
      assertEquals("********", author.getPassword());
      assertEquals("sally@ibatis.apache.org", author.getEmail());
      assertEquals(null, author.getBio());
    } catch (ExecutorException e) {
      if (executor instanceof CachingExecutor) {
        // TODO see issue #464. Fail is OK.
        assertTrue(e.getMessage().contains("OUT params is not supported"));
      } else {
        throw e;
      }
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldFetchPostsForBlog() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
      MappedStatement selectPosts = ExecutorTestHelper.prepareSelectPostsForBlogMappedStatement(config);
      config.addMappedStatement(selectBlog);
      config.addMappedStatement(selectPosts);
      List<Post> posts = executor.query(selectPosts, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      assertEquals(2, posts.size());
      assertTrue(posts.get(1) instanceof Factory);
      assertNotNull(posts.get(1).getBlog());
      assertEquals(1, posts.get(1).getBlog().getId());
      executor.rollback(true);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldFetchOneOrphanedPostWithNoBlog() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
      MappedStatement selectPost = ExecutorTestHelper.prepareSelectPostMappedStatement(config);
      config.addMappedStatement(selectBlog);
      config.addMappedStatement(selectPost);
      List<Post> posts = executor.query(selectPost, 5, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(1, posts.size());
      Post post = posts.get(0);
      assertNull(post.getBlog());
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldFetchPostWithBlogWithCompositeKey() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectBlog = ExecutorTestHelper.prepareSelectBlogByIdAndAuthor(config);
      MappedStatement selectPost = ExecutorTestHelper.prepareSelectPostWithBlogByAuthorMappedStatement(config);
      config.addMappedStatement(selectBlog);
      config.addMappedStatement(selectPost);
      List<Post> posts = executor.query(selectPost, 2, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      assertEquals(1, posts.size());
      Post post = posts.get(0);
      assertNotNull(post.getBlog());
      assertEquals(101, post.getBlog().getAuthor().getId());
      executor.rollback(true);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldFetchComplexBlogs() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
      MappedStatement selectPosts = ExecutorTestHelper.prepareSelectPostsForBlogMappedStatement(config);
      config.addMappedStatement(selectBlog);
      config.addMappedStatement(selectPosts);
      List<Blog> blogs = executor.query(selectBlog, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      assertEquals(1, blogs.size());
      assertNotNull(blogs.get(0).getPosts());
      assertEquals(2, blogs.get(0).getPosts().size());
      assertEquals(1, blogs.get(0).getPosts().get(1).getBlog().getPosts().get(1).getBlog().getId());
      executor.rollback(true);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldMapConstructorResults() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatementWithConstructorResults(config);
      List<Author> authors = executor.query(selectStatement, 102, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      assertEquals(1, authors.size());

      Author author = authors.get(0);
      assertEquals(102, author.getId());
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  @Test
  public void shouldClearDeferredLoads() throws Exception {
    
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
      MappedStatement selectPosts = ExecutorTestHelper.prepareSelectPostsForBlogMappedStatement(config);
      config.addMappedStatement(selectBlog);
      config.addMappedStatement(selectPosts);
      MappedStatement selectAuthor = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      MappedStatement insertAuthor = ExecutorTestHelper.prepareInsertAuthorMappedStatement(config);

      //generate DeferredLoads
      executor.query(selectPosts, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

      Author author = new Author(-1, "someone", "******", "someone@apache.org", null, Section.NEWS);
      executor.update(insertAuthor, author);
      executor.query(selectAuthor, -1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
    } finally {
      executor.rollback(true);
      executor.close(false);
    }
  }

  protected Executor createExecutor(Transaction transaction) {
    return new SimpleExecutor(config,transaction);
  }

}
