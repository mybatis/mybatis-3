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
package org.apache.ibatis.submitted.strict_collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StrictCollectionTypeCheckTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/strict_collection/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/strict_collection/CreateDB.sql");
  }

  @Test
  void correctOfTypePassesWhenStrictCheckEnabled() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/strict_collection/mybatis-config.xml")) {
      SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
      factory.getConfiguration().setStrictResultMapCollectionTypeCheck(true);
      factory.getConfiguration().addMapper(StrictCollectionMapper.class);

      try (SqlSession sqlSession = factory.openSession()) {
        StrictCollectionMapper mapper = sqlSession.getMapper(StrictCollectionMapper.class);
        List<Group> groups = mapper.getGroups();
        assertEquals(2, groups.size());
        assertEquals(2, groups.get(0).getUsers().size());
        assertEquals("User 1", groups.get(0).getUsers().get(0).getName());
        assertEquals(1, groups.get(1).getUsers().size());
      }
    }
  }

  @Test
  void wrongOfTypeThrowsBuilderExceptionWhenStrictCheckEnabled() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/strict_collection/mybatis-config.xml")) {
      SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
      factory.getConfiguration().setStrictResultMapCollectionTypeCheck(true);

      BuilderException ex = assertThrows(BuilderException.class,
          () -> factory.getConfiguration().addMapper(StrictCollectionWrongMapper.class));
      assertTrue(ex.getMessage().contains("Collection property 'users'"));
      assertTrue(ex.getMessage().contains("ofType"));
    }
  }

  @Test
  void wrongOfTypePassesWhenStrictCheckDisabled() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/strict_collection/mybatis-config.xml")) {
      SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
      // strict check is disabled by default
      factory.getConfiguration().addMapper(StrictCollectionWrongMapper.class);

      try (SqlSession sqlSession = factory.openSession()) {
        StrictCollectionWrongMapper mapper = sqlSession.getMapper(StrictCollectionWrongMapper.class);
        List<Group> groups = mapper.getGroups();
        assertEquals(2, groups.size());
      }
    }
  }
}
