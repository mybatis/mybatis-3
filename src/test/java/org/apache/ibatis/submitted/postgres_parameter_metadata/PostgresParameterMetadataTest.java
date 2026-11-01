/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.postgres_parameter_metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.PgContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @see <a href="https://github.com/mybatis/mybatis-3/issues/3829">mybatis-3 #3829</a>
 * @see <a href="https://github.com/pgjdbc/pgjdbc/issues/4505">pgjdbc #4505</a>
 */
@Tag("TestcontainersTests")
class PostgresParameterMetadataTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        PgContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  void booleanParameterWithoutCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    sqlSessionFactory.getConfiguration().setIgnoreParameterMetaData(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectOne();
      assertEquals("true", mapper.selectJsonTextForImplicitParameter(Boolean.TRUE));
    } finally {
      sqlSessionFactory.getConfiguration().setIgnoreParameterMetaData(false);
    }
  }

  @Test
  void stringParameterWithoutCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    sqlSessionFactory.getConfiguration().setIgnoreParameterMetaData(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectOne();
      assertEquals("\"mybatis\"", mapper.selectJsonTextForImplicitParameter("mybatis"));
    } finally {
      sqlSessionFactory.getConfiguration().setIgnoreParameterMetaData(false);
    }
  }

  @Test
  void booleanParameterWithExplicitJdbcTypeIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectOne();
      assertEquals("true", mapper.selectJsonTextForBooleanJdbcTypeParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithExplicitJdbcTypeIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectOne();
      assertEquals("\"mybatis\"", mapper.selectJsonTextForVarcharJdbcTypeParameter("mybatis"));
    }
  }

  @Test
  void booleanParameterWithExplicitCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectOne();
      assertEquals("true", mapper.selectJsonTextForBooleanCastParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithExplicitCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectOne();
      assertEquals("\"mybatis\"", mapper.selectJsonTextForTextCastParameter("mybatis"));
    }
  }

  @Test
  void booleanParameterWithoutCastIsSerializedAsJsonInAutoCommitSession() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("true", mapper.selectJsonTextForImplicitParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithoutCastIsSerializedAsJsonInAutoCommitSession() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("\"mybatis\"", mapper.selectJsonTextForImplicitParameter("mybatis"));
    }
  }

  @Test
  void booleanParameterWithoutCastIsSerializedAsJsonBeforeTransactionIsStarted() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("true", mapper.selectJsonTextForImplicitParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithoutCastIsSerializedAsJsonBeforeTransactionIsStarted() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("\"mybatis\"", mapper.selectJsonTextForImplicitParameter("mybatis"));
    }
  }

  interface Mapper {
    @Select("SELECT 1")
    String selectOne();

    @Select("SELECT to_jsonb(#{value})::text")
    String selectJsonTextForImplicitParameter(Object value);

    @Select("SELECT to_jsonb(#{value,jdbcType=BOOLEAN})::text")
    String selectJsonTextForBooleanJdbcTypeParameter(Object value);

    @Select("SELECT to_jsonb(#{value,jdbcType=VARCHAR})::text")
    String selectJsonTextForVarcharJdbcTypeParameter(Object value);

    @Select("SELECT to_jsonb(CAST(#{value} AS BOOLEAN))::text")
    String selectJsonTextForBooleanCastParameter(Object value);

    @Select("SELECT to_jsonb(CAST(#{value} AS TEXT))::text")
    String selectJsonTextForTextCastParameter(Object value);
  }
}
