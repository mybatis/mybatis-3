/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.raw_sql_source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.util.stream.Stream;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RawSqlSourceTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/raw_sql_source/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/raw_sql_source/CreateDB.sql");
  }

  @Test
  void shouldUseRawSqlSourceForAnStaticStatement() {
    test("getUser1", RawSqlSource.class);
  }

  @Test
  void shouldUseDynamicSqlSourceForAnStatementWithInlineArguments() {
    test("getUser2", DynamicSqlSource.class);
  }

  @Test
  void shouldUseDynamicSqlSourceForAnStatementWithXmlTags() {
    test("getUser3", DynamicSqlSource.class);
  }

  private void test(String statement, Class<? extends SqlSource> sqlSource) {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Assertions.assertEquals(sqlSource,
          sqlSession.getConfiguration().getMappedStatement(statement).getSqlSource().getClass());
      String sql = sqlSession.getConfiguration().getMappedStatement(statement).getSqlSource().getBoundSql('?').getSql();
      Assertions.assertEquals("select * from users where id = ?", sql);
      User user = sqlSession.selectOne(statement, 1);
      Assertions.assertEquals("User1", user.getName());
    }
  }

  @MethodSource
  @ParameterizedTest
  void testShrinkWhitespacesInSql(String input, boolean shrinkWhitespaces, String expected) {
    Configuration config = new Configuration();
    config.setShrinkWhitespacesInSql(shrinkWhitespaces);
    String actual = new RawSqlSource(config, input, null).getBoundSql(null).getSql();
    assertEquals(expected, actual);
  }

  static Stream<Arguments> testShrinkWhitespacesInSql() {
    return Stream.of(
        Arguments.arguments("\t\n\n  SELECT * \n        FROM user\n \t        WHERE user_id = 1\n\t  ", false,
            "\t\n\n  SELECT * \n        FROM user\n \t        WHERE user_id = 1\n\t  "),
        Arguments.arguments("\t\n\n SELECT * \n FROM user\n \t WHERE user_id = 1\n\t", true,
            "SELECT * FROM user WHERE user_id = 1"));
  }

  @MethodSource
  @ParameterizedTest
  void testShrinkWhitespacesInSql_SqlNode(SqlNode input, boolean shrinkWhitespaces, String expected) {
    Configuration config = new Configuration();
    config.setShrinkWhitespacesInSql(shrinkWhitespaces);
    String actual = new RawSqlSource(config, input, null).getBoundSql(null).getSql();
    assertEquals(expected, actual);
  }

  static Stream<Arguments> testShrinkWhitespacesInSql_SqlNode() {
    return Stream.of(
        Arguments.arguments(
            new StaticTextSqlNode("\t\n\n  SELECT * \n        FROM user\n \t        WHERE user_id = 1\n\t  "), false,
            "SELECT * \n        FROM user\n \t        WHERE user_id = 1"),
        Arguments.arguments(new StaticTextSqlNode("\t\n\n SELECT * \n FROM user\n \t WHERE user_id = 1\n\t"), true,
            "SELECT * FROM user WHERE user_id = 1"));
  }
}
