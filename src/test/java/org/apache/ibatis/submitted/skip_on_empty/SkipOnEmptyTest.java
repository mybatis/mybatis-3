/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.skip_on_empty;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SkipOnEmptyTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/skip_on_empty/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
//    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
//      "org/apache/ibatis/submitted/skip_on_empty/CreateDB.sql");
    // no table created here, if the execution is not skipped,
    // an exception will be thrown(because there is no table named 'users'), so we can validate the skip logic
  }

  @Test
  void shouldGetEmptyListPassingAnEmptyList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<Integer> ids = new ArrayList<>();
      List<User> users = mapper.getUsersListFromList(ids);
      Assertions.assertEquals(0, users.size());
    }
  }

  @Test
  void shouldGetEmptyArrayListPassingAnEmptyArray() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Integer[] ids = new Integer[]{};
      ArrayList<User> users = mapper.getUsersArrayListFromArray(ids);
      Assertions.assertEquals(0, users.size());
    }
  }

  @Test
  void shouldGetEmptyLinkedListPassingAnEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Request request = new Request();
      HashSet<Integer> ids = new HashSet<>();
      request.setIdSet(ids);
      LinkedList<User> users = mapper.getUsersLinkedListFromRequest(request);
      Assertions.assertEquals(0, users.size());
    }
  }

  @Test
  void shouldGetNullPassingAnEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Request.RequestHolder holder = new Request.RequestHolder();
      Request request = new Request();
      Set<Integer> ids = new HashSet<>();
      request.setIdSet(ids);
      holder.setRef(request);
      User firstUser = mapper.getFirstUserFromList(holder);
      Assertions.assertNull(firstUser);
    }
  }

  @Test
  void shouldGetEmptyMapPassingAnEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<Integer> ids = new ArrayList<>();
      ids.add(1);
      ids.add(2);
      ArrayList<String> codes = new ArrayList<>();
      Map<String, User> codeMap = mapper.getCodeMapFromIdsAndCodes(ids, codes);
      Assertions.assertEquals(0, codeMap.size());
    }
  }

  @Test
  void shouldThrowExceptionPassingAnEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<Integer> ids = new ArrayList<>();
      Assertions.assertThrows(BindingException.class, () -> mapper.getUserCursorFromList(ids));
    }
  }

  @Test
  void shouldGetEmptyArrayPassingAnEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Request request = new Request();
      HashSet<Integer> ids = new HashSet<>();
      request.setIdSet(ids);
      User[] users = mapper.getUsersArrayFromNamedRequest(request);
      Assertions.assertEquals(0, users.length);
    }
  }

  @Test
  void shouldGetZeroPassingAnEmptyCollectionToInsert() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<User> users = new ArrayList<>();
      int effected = mapper.batchInsert(users);
      Assertions.assertEquals(0, effected);
    }
  }

  @Test
  void shouldGetZeroPassingAnEmptyCollectionToUpdate() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<Integer> ids = new ArrayList<>();
      int effected = mapper.batchUpdate(ids, "newName");
      Assertions.assertEquals(0, effected);
    }
  }

  @Test
  void shouldGetZeroPassingAnEmptyCollectionToDelete() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<Integer> ids = new ArrayList<>();
      int effected = mapper.batchDelete(ids);
      Assertions.assertEquals(0, effected);
    }
  }

  @Test
  void shouldNotCallTheHandlerPassingAnEmptyCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      ArrayList<Integer> ids = new ArrayList<>();
      mapper.getUsersListFromListWithHandler(ids,
        resultContext -> Assertions.fail("The ResultHandler should not be called"));
    }
  }

}
