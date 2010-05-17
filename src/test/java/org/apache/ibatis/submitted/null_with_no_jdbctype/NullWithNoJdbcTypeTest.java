package org.apache.ibatis.submitted.null_with_no_jdbctype;

import domain.blog.Author;
import domain.blog.Section;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.*;

public class NullWithNoJdbcTypeTest extends BaseDataTest {

  private static interface JPetStoreMapper {
    @Insert("INSERT INTO category (catid, name, descn) VALUES (#{id},#{name},#{description})")
    int insertCategory(@Param("id") String id, @Param("name") String name, @Param("description") String description);
  }

  @Test
  public void shouldSucceedAddingRowWithNullValueWithHSQLDB() throws Exception {
    DataSource ds = BaseDataTest.createJPetstoreDataSource();
    Environment env = new Environment("test",new JdbcTransactionFactory(),ds);
    Configuration config = new Configuration(env);
    config.addMapper(JPetStoreMapper.class);

    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(config);
    SqlSession session = sqlMapper.openSession();
    try {
      JPetStoreMapper mapper = session.getMapper(JPetStoreMapper.class);
      int n = mapper.insertCategory("MONKEYS",null,"Big hairy friendly (sometimes) mammals...");
      assertEquals(1,n);
      session.rollback();
    } finally {
      if (session != null) session.close();
    }
  }

  private static interface BlogMapper {
    @Insert("insert into " +
        "Author (id,username,password,email,bio,favourite_section) " +
        "values(#{id}, #{username}, #{password}, #{email}, #{bio}, #{favouriteSection})")
    int insertAuthor(Author author);
  }

  @Test
  public void shouldParseMapWithIntegerJdbcType() throws Exception {
    DataSource ds = BaseDataTest.createBlogDataSource();
    Environment env = new Environment("test",new JdbcTransactionFactory(),ds);
    Configuration config = new Configuration(env);
    config.addMapper(BlogMapper.class);

    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(config);
    SqlSession session = sqlMapper.openSession();
    try {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      int n = 0;
      try {
        n = mapper.insertAuthor(new Author(99999,"barney","******","barney@iloveyou.com",null, Section.NEWS));
        fail("Expected exception.");
      } catch (Exception e) {
        assertTrue(e.getMessage().contains("Most JDBC drivers require"));
      }
      session.rollback();
    } finally {
      if (session != null) session.close();
    }
  }


}
