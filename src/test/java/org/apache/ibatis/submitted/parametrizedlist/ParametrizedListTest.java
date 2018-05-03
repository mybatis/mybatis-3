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
package org.apache.ibatis.submitted.parametrizedlist;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.junit.Assert;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class ParametrizedListTest {

  private SqlSessionFactory sqlSessionFactory;

  @Before
  public void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/parametrizedlist/Config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/parametrizedlist/CreateDB.sql");
  }

  @Test
  public void testShouldDetectUsersAsParameterInsideAList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User<String>> list = mapper.getAListOfUsers();
      Assert.assertEquals(User.class, list.get(0).getClass());
    }
  }

  @Test
  public void testShouldDetectUsersAsParameterInsideAMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<Integer, User<String>> map = mapper.getAMapOfUsers();
      Assert.assertEquals(User.class, map.get(1).getClass());
    }
  }

  @Test
  public void testShouldGetAUserAsAMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Map<String, Object> map = mapper.getUserAsAMap();
      Assert.assertEquals(1, map.get("ID"));
    }
  }

  @Test
  public void testShouldGetAListOfMaps() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Map<String, Object>> map = mapper.getAListOfMaps();
      Assert.assertEquals(1, map.get(0).get("ID"));
    }
  }

}
