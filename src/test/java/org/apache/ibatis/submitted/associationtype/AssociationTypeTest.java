/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.submitted.associationtype;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AssociationTypeTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/associationtype/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/associationtype/CreateDB.sql");
  }

  @ParameterizedTest
  @EnumSource
  void shouldGetAUser(LocalCacheScope localCacheScope) {
    sqlSessionFactory.getConfiguration().setLocalCacheScope(localCacheScope);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Map<String, ?>> results = sqlSession.selectList("getUser");
      for (Map<String, ?> r : results) {
        Object a1 = r.get("a1");
        Object a2 = r.get("a2");
        Assertions.assertEquals(String.class, a1.getClass());
        Assertions.assertEquals(String.class, a2.getClass());
        Assertions.assertSame(a1, a2,
            "The result should be put into local cache regardless of localCacheScope setting.");
      }
    } finally {
      // Reset the scope for other tests
      sqlSessionFactory.getConfiguration().setLocalCacheScope(LocalCacheScope.SESSION);
    }
  }

}
