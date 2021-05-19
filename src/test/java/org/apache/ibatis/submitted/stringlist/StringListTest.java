/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.stringlist;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StringListTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/stringlist/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/stringlist/CreateDB.sql");
  }

  @Test
  void shouldMapListOfStrings() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersAndGroups(1);
      Assertions.assertEquals(1, users.size());
      Assertions.assertEquals(2, users.get(0).getGroups().size());
      Assertions.assertEquals(2, users.get(0).getRoles().size());
    }
  }

  @Test
  void shouldMapListOfStringsToMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Map<String, Object>> results = mapper.getUsersAndGroupsMap(1);
      Assertions.assertEquals(1, results.size());
      Assertions.assertEquals(2, ((List<?>) results.get(0).get("groups")).size());
      Assertions.assertEquals(2, ((List<?>) results.get(0).get("roles")).size());
    }
  }

  @Test
  void shouldFailFastIfCollectionTypeIsAmbiguous() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/stringlist/mybatis-config-invalid.xml")) {
      new SqlSessionFactoryBuilder().build(reader);
      fail("Should throw exception when collection type is unresolvable.");
    } catch (PersistenceException e) {
      assertTrue(e.getMessage()
          .contains("Ambiguous collection type for property 'groups'. You must specify 'javaType' or 'resultMap'."));
    }
  }
}
