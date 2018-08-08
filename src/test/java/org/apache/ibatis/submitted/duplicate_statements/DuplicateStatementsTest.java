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
package org.apache.ibatis.submitted.duplicate_statements;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DuplicateStatementsTest {

  private SqlSessionFactory sqlSessionFactory;

  @Before
  public void setupDb() throws Exception {
      // create a SqlSessionFactory
      try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/duplicate_statements/mybatis-config.xml")) {
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      }

      // populate in-memory database
      BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
              "org/apache/ibatis/submitted/duplicate_statements/CreateDB.sql");
  }

  @Test
  public void shouldGetAllUsers() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getAllUsers();
      Assert.assertEquals(10, users.size());
    }
  }

  @Test
  public void shouldGetFirstFourUsers() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getAllUsers(new RowBounds(0, 4));
      Assert.assertEquals(4, users.size());
    }
  }

  @Test
  @Ignore("fails currently - issue 507")
  public void shouldGetAllUsers_Annotated() {
    sqlSessionFactory.getConfiguration().addMapper(AnnotatedMapper.class);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      List<User> users = mapper.getAllUsers();
      Assert.assertEquals(10, users.size());
    }
  }

  @Test
  @Ignore("fails currently - issue 507")
  public void shouldGetFirstFourUsers_Annotated() {
    sqlSessionFactory.getConfiguration().addMapper(AnnotatedMapper.class);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      List<User> users = mapper.getAllUsers(new RowBounds(0, 4));
      Assert.assertEquals(4, users.size());
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public void shouldFailForDuplicateMethod() {
    sqlSessionFactory.getConfiguration().addMapper(AnnotatedMapperExtended.class);
  }
}
