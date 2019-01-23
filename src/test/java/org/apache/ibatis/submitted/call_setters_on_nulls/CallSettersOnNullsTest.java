/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.call_setters_on_nulls;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.List;
import java.util.Map;

class CallSettersOnNullsTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/call_setters_on_nulls/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/call_setters_on_nulls/CreateDB.sql");
  }

  @Test
  void shouldCallNullOnMappedProperty() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserMapped(1);
      Assertions.assertTrue(user.nullReceived);
    }
  }

  @Test
  void shouldCallNullOnAutomaticMapping() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserUnmapped(1);
      Assertions.assertTrue(user.nullReceived);
    }
  }

  @Test
  void shouldCallNullOnMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map user = mapper.getUserInMap(1);
      Assertions.assertTrue(user.containsKey("NAME"));
    }
  }

  @Test
  void shouldCallNullOnMapForSingleColumn() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Map<String, Object>> oneColumns = mapper.getNameOnly();
      // When callSetterOnNull is true, setters are called with null values
      // but if all the values for an object are null
      // the object itself should be null (same as default behaviour)
      Assertions.assertNull(oneColumns.get(1));
    }
  }

  @Test
  void shouldCallNullOnMapForSingleColumnWithResultMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Map<String, Object>> oneColumns = mapper.getNameOnlyMapped();
      // Assertions.assertNotNull(oneColumns.get(1));
      // TEST changed after fix for #307
      // When callSetterOnNull is true, setters are called with null values
      // but if all the values for an object are null
      // the object itself should be null (same as default behaviour)
      Assertions.assertNull(oneColumns.get(1));
    }
  }

}
