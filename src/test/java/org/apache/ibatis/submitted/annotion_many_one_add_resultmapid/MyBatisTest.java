package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;

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
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/annotion_many_one_add_resultmapid/SqlMapConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/annotion_many_one_add_resultmapid/CreateDB.sql");
  }

  @Test
  void test() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.flindAll();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
      Assertions.assertEquals(2, users.get(0).getRoles().size(), "should have 2 roles");
    }
  }

  @Test
  void test2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.flindAll2();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(4, users.size(), "should return 4 results");
      Assertions.assertEquals(2, users.get(0).getRoles().size(), "should have 2 roles");
    }
  }

  @Test
  void test3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.flindAll3();
      Assertions.assertNotNull(users, "users must not be null");
      Assertions.assertEquals(2, users.size(), "should return 2 results");
      Assertions.assertEquals(1, users.get(0).getRoles().size(), "should have 1 roles");
      Assertions.assertEquals("老师", users.get(0).getRoles().get(0).getRoleName(), "should have 1 roles");
    }
  }

}
