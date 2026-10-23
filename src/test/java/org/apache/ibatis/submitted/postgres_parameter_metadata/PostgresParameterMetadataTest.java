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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @see <a href="https://github.com/mybatis/mybatis-3/issues/3829">Issue #3829</a>
 */
@Tag("TestcontainersTests")
class PostgresParameterMetadataTest {

  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() {
    POSTGRES.start();
    UnpooledDataSource dataSource = new UnpooledDataSource("org.postgresql.Driver", POSTGRES.getJdbcUrl(),
        POSTGRES.getUsername(), POSTGRES.getPassword());
    Configuration configuration = new Configuration();
    configuration.setEnvironment(new Environment("development", new JdbcTransactionFactory(), dataSource));
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @AfterAll
  static void tearDown() {
    POSTGRES.stop();
  }

  @Test
  void booleanParameterWithoutCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      startTransaction(sqlSession);
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("true", mapper.selectJsonTextForImplicitParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithoutCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      startTransaction(sqlSession);
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("\"mybatis\"", mapper.selectJsonTextForImplicitParameter("mybatis"));
    }
  }

  @Test
  void booleanParameterWithExplicitJdbcTypeIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      startTransaction(sqlSession);
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("true", mapper.selectJsonTextForBooleanJdbcTypeParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithExplicitJdbcTypeIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      startTransaction(sqlSession);
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("\"mybatis\"", mapper.selectJsonTextForVarcharJdbcTypeParameter("mybatis"));
    }
  }

  @Test
  void booleanParameterWithExplicitCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      startTransaction(sqlSession);
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("true", mapper.selectJsonTextForBooleanCastParameter(Boolean.TRUE));
    }
  }

  @Test
  void stringParameterWithExplicitCastIsSerializedAsJsonInsideActiveTransaction() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      startTransaction(sqlSession);
      Mapper mapper = sqlSession.getMapper(Mapper.class);
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

  /**
   * Executes a statement on the session connection so that the connection already carries an open transaction before
   * any mapper statement is prepared and parameterized.
   */
  private static void startTransaction(SqlSession sqlSession) throws SQLException {
    assertFalse(sqlSession.getConnection().getAutoCommit());
    try (Statement statement = sqlSession.getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT 1")) {
      assertTrue(resultSet.next());
    }
  }

  interface Mapper {

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
