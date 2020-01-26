package org.apache.ibatis.submitted.expand_collection_param;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.expand_collection_param.mapper.ProviderMapper;
import org.apache.ibatis.submitted.expand_collection_param.model.User;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionInProviderlTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/expand_collection_param/config/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().getMapperRegistry().addMapper(ProviderMapper.class);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/expand_collection_param/config/CreateDB.sql");
  }

  @Test
  public void shouldQueryByCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProviderMapper mapper = sqlSession.getMapper(ProviderMapper.class);
      List<User> users = mapper.getUsers(6, Arrays.asList(UserRole.ADMIN, UserRole.REVIEWER));
      assertEquals(3, users.size());
      assertEquals(3, users.get(0).getId());
      assertEquals(4, users.get(1).getId());
      assertEquals(5, users.get(2).getId());
    }
  }

  @Test
  public void shouldQueryByEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProviderMapper mapper = sqlSession.getMapper(ProviderMapper.class);
      List<User> users = mapper.getUsers(6, new ArrayList<>());
      assertEquals(5, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(2, users.get(1).getId());
      assertEquals(3, users.get(2).getId());
      assertEquals(4, users.get(3).getId());
      assertEquals(5, users.get(4).getId());
    }
  }

}
