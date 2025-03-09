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
package org.apache.ibatis.submitted.auto_type_from_non_ambiguous_constructor;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutoTypeFromNonAmbiguousConstructorTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/auto_type_from_non_ambiguous_constructor/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/auto_type_from_non_ambiguous_constructor/CreateDB.sql");
  }

  @Test
  void testNormalCaseWhereAllTypesAreProvided() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Account account = mapper.getAccountNonAmbiguous(1);
      Assertions.assertThat(account).isNotNull();
      Assertions.assertThat(account.accountId()).isEqualTo(1);
      Assertions.assertThat(account.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account.accountType()).isEqualTo("Current");
    }
  }

  @Test
  void testNoTypesAreProvidedAndUsesAutoType() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Account account = mapper.getAccountJavaTypesMissing(1);
      Assertions.assertThat(account).isNotNull();
      Assertions.assertThat(account.accountId()).isEqualTo(1);
      Assertions.assertThat(account.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account.accountType()).isEqualTo("Current");

      Mapper1 mapper1 = sqlSession.getMapper(Mapper1.class);
      Account account1 = mapper1.getAccountJavaTypesMissing(1);
      Assertions.assertThat(account1).isNotNull();
      Assertions.assertThat(account1.accountId()).isEqualTo(1);
      Assertions.assertThat(account1.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account1.accountType()).isEqualTo("Current");
    }
  }

  @Test
  void testSucceedsWhenConstructorWithMoreTypesAreFound() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Account2 account = mapper.getAccountExtraParameter(1);
      Assertions.assertThat(account).isNotNull();
      Assertions.assertThat(account.accountId()).isEqualTo(1);
      Assertions.assertThat(account.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account.accountType()).isEqualTo("Current");
    }
  }

  @Test
  void testChoosesCorrectConstructorWhenPartialTypesAreProvided() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Account3 account = mapper.getAccountPartialTypesProvided(1);
      Assertions.assertThat(account).isNotNull();
      Assertions.assertThat(account.accountId()).isEqualTo(1);
      Assertions.assertThat(account.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account.accountType()).isEqualTo("Current");

      Mapper1 mapper1 = sqlSession.getMapper(Mapper1.class);
      Account3 account1 = mapper1.getAccountPartialTypesProvided(1);
      Assertions.assertThat(account1).isNotNull();
      Assertions.assertThat(account1.accountId()).isEqualTo(1);
      Assertions.assertThat(account1.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account1.accountType()).isEqualTo("Current");
    }
  }

  @Test
  void testSucceedsWhenConstructorArgsAreInWrongOrderAndTypesAreNotProvided() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Account4 account = mapper.getAccountWrongOrder(1);
      Assertions.assertThat(account).isNotNull();
      Assertions.assertThat(account.accountId()).isEqualTo(1);
      Assertions.assertThat(account.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account.accountDob()).isEqualTo("2025-01-05");

      Mapper1 mapper1 = sqlSession.getMapper(Mapper1.class);
      Account4 account4 = mapper1.getAccountWrongOrder(1);
      Assertions.assertThat(account4).isNotNull();
      Assertions.assertThat(account4.accountId()).isEqualTo(1);
      Assertions.assertThat(account4.accountName()).isEqualTo("Account 1");
      Assertions.assertThat(account4.accountDob()).isEqualTo("2025-01-05");
    }
  }
}
