package org.apache.ibatis.submitted.initialized_collection_property.test;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class AuthorDAO {
  private SqlSessionFactory sqlSessionFactory;

  public AuthorDAO(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }

  public List<Author> getAuthors() {
    SqlSession session = sqlSessionFactory.openSession();
    List<Author> authors = session.selectList("Author.getAllAuthors");
    session.close();
    return authors;
  }

  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }
}
