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
package org.apache.ibatis.submitted.usesjava8.optional_on_mapper_method;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Reader;
import java.sql.Connection;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

/**
 * Tests for support the {@code java.util.Optional} as return type of mapper method.
 *
 * @since 3.5.0
 * @author Kazuki Shimizu
 */
public class OptionalOnMapperMethodTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/usesjava8/optional_on_mapper_method/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/usesjava8/optional_on_mapper_method/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void returnNotNullOnAnnotation() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingAnnotation(1);
      assertTrue(user.isPresent());
      assertThat(user.get().getName(), is("User1"));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void returnNullOnAnnotation() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingAnnotation(3);
      assertFalse(user.isPresent());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void returnNotNullOnXml() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingXml(2);
      assertTrue(user.isPresent());
      assertThat(user.get().getName(), is("User2"));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void returnNullOnXml() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingXml(3);
      assertFalse(user.isPresent());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void returnOptionalFromSqlSession() {
    SqlSession sqlSession = Mockito.spy(sqlSessionFactory.openSession());

    User mockUser = new User();
    mockUser.setName("mock user");
    Optional<User> optionalMockUser = Optional.of(mockUser);
    doReturn(optionalMockUser).when(sqlSession).selectOne(any(String.class), any(Object.class));

    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingAnnotation(3);
      assertTrue(user == optionalMockUser);
    } finally {
      sqlSession.close();
    }
  }

}
