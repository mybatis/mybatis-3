package org.apache.ibatis.submitted.expand_collection_param;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.expand_collection_param.mapper.AnnotationMapper;
import org.apache.ibatis.submitted.expand_collection_param.model.User;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;
import org.apache.ibatis.submitted.expand_collection_param.model.UserStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CollectionInAnnotationTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/expand_collection_param/config/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().getMapperRegistry().addMapper(AnnotationMapper.class);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/expand_collection_param/config/CreateDB.sql");
  }

  @Test
  public void shouldQueryByIntCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      List<User> users = mapper.getUsersByIds(Arrays.asList(1, 2, 4));
      assertEquals(3, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(2, users.get(1).getId());
      assertEquals(4, users.get(2).getId());
    }
  }

  @Test
  public void shouldQueryByIntArray() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      List<User> users = mapper.getUsersByArrayIds(new int[]{1, 8, 5});
      assertEquals(2, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(5, users.get(1).getId());
    }
  }


  @Test
  public void shouldQueryByStringCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      List<User> users = mapper.getUsersByNames(Arrays.asList("John", "Mark", "Bill"));
      assertEquals(4, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(4, users.get(1).getId());
      assertEquals(5, users.get(2).getId());
      assertEquals(6, users.get(3).getId());
    }
  }

  @Test
  public void shouldQueryByEnumCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      List<User> users = mapper.getUsersByRoles(Arrays.asList(UserRole.REVIEWER, UserRole.WRITER));
      assertEquals(4, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(2, users.get(1).getId());
      assertEquals(3, users.get(2).getId());
      assertEquals(4, users.get(3).getId());
    }
  }

  @Test
  public void shouldQueryByOrdinalEnumCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      List<User> users = mapper.getUsersByStatus(Arrays.asList(UserStatus.NOT_ACTIVATED, UserStatus.DELETED));
      assertEquals(4, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(3, users.get(1).getId());
      assertEquals(4, users.get(2).getId());
      assertEquals(6, users.get(3).getId());
    }
  }

  @Test
  public void shouldQueryByMultiplesLists() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      List<UserStatus> status = Arrays.asList(UserStatus.NOT_ACTIVATED, UserStatus.DELETED);
      List<UserRole> roles = Arrays.asList(UserRole.WRITER, UserRole.ADMIN);
      List<User> users = mapper.getUsersByStatusAndRoles(status, roles);
      assertEquals(2, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals(6, users.get(1).getId());
    }
  }

}
