package org.apache.ibatis.submitted.add_localCacheDetails;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.List;

class MyBatisTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/add_localCacheDetails/SqlMapConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/add_localCacheDetails/CreateDB.sql");
  }

  @Test
  void testFindAll() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll();
      users = mapper.findAll();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
    }
  }

  @Test
  void testFindAll2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll2();
      users = mapper.findAll2();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
    }
  }

  @Test
  void testFindAll3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll3();
      users = mapper.findAll3();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
    }
  }

  @Test
  void testFindAll4() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll4();
      users = mapper.findAll4();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
    }
  }

  @Test
  void testFindAll5() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll5();
      users = mapper.findAll5();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
    }
  }

}
