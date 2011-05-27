package org.apache.ibatis.binding;

import domain.blog.*;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import org.junit.Ignore;

public class BindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    sqlSessionFactory = new IbatisConfig().getSqlSessionFactory();
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
      List<Map> blogs = mapper.selectBlogsAsMaps();
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
  public void shouldSelectOneBlogAsMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map blog = mapper.selectBlogAsMap(new HashMap() {
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
      assertTrue(posts.get(1) instanceof Post);
      assertFalse(posts.get(1) instanceof DraftPost);
      assertTrue(posts.get(2) instanceof DraftPost);
      assertTrue(posts.get(3) instanceof Post);
      assertFalse(posts.get(3) instanceof DraftPost);
      assertTrue(posts.get(4) instanceof Post);
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
      assertTrue(posts.get(1) instanceof Post);
      assertFalse(posts.get(1) instanceof DraftPost);
      assertTrue(posts.get(2) instanceof DraftPost);
      assertTrue(posts.get(3) instanceof Post);
      assertFalse(posts.get(3) instanceof DraftPost);
      assertTrue(posts.get(4) instanceof Post);
      assertFalse(posts.get(4) instanceof DraftPost);
    } finally {
      session.close();
    }
  }


}
