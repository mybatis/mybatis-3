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
package org.apache.ibatis.submitted.collection_in_constructor;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CollectionInConstructorTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/collection_in_constructor/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/collection_in_constructor/CreateDB.sql");
  }

  @Test
  void testSimple() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store store = mapper.getAStore(1);
      List<Isle> isles = store.getIsles();
      Assertions.assertIterableEquals(
          Arrays.asList(new Isle(101, "Isle 101"), new Isle(102, "Isle 102"), new Isle(103, "Isle 103")), isles);
    }
  }

  @Test
  void testSimpleList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Store> stores = mapper.getStores();
      Assertions.assertIterableEquals(
          Arrays.asList(new Isle(101, "Isle 101"), new Isle(102, "Isle 102"), new Isle(103, "Isle 103")),
          stores.get(0).getIsles());
      Assertions.assertTrue(stores.get(1).getIsles().isEmpty());
      Assertions.assertIterableEquals(Arrays.asList(new Isle(104, "Isle 104"), new Isle(105, "Isle 105")),
          stores.get(2).getIsles());
    }
  }

  @Test
  void shouldEmptyListBeReturned() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertTrue(mapper.getAStore(2).getIsles().isEmpty());
    }
  }

  @Test
  void testTwoLists() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store2 store = mapper.getAStore2(1);
      List<Clerk> clerks = store.getClerks();
      List<Isle> isles = store.getIsles();
      Assertions.assertIterableEquals(Arrays.asList(new Clerk(1001, "Clerk 1001"), new Clerk(1002, "Clerk 1002"),
          new Clerk(1003, "Clerk 1003"), new Clerk(1004, "Clerk 1004"), new Clerk(1005, "Clerk 1005")), clerks);
      Assertions.assertIterableEquals(
          Arrays.asList(new Isle(101, "Isle 101"), new Isle(102, "Isle 102"), new Isle(103, "Isle 103")), isles);
    }
  }

  @Test
  void testListOfStrings() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store3 store = mapper.getAStore3(1);
      List<String> isleNames = store.getIsleNames();
      Assertions.assertEquals(3, isleNames.size());
      Assertions.assertIterableEquals(Arrays.asList("Isle 101", "Isle 102", "Isle 103"), isleNames);
    }
  }

  @Test
  void testObjectWithBuilder() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store4 store = mapper.getAStore4(1);
      List<Isle> isles = store.getIsles();
      Assertions.assertIterableEquals(
          Arrays.asList(new Isle(101, "Isle 101"), new Isle(102, "Isle 102"), new Isle(103, "Isle 103")), isles);
    }
  }

  @Test
  void testTwoListsOfSameResultMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store5 store = mapper.getAStore5(1);
      List<Clerk> clerks = store.getClerks();
      List<Clerk> managers = store.getManagers();
      Assertions.assertIterableEquals(Arrays.asList(new Clerk(1001, "Clerk 1001"), new Clerk(1002, "Clerk 1002"),
          new Clerk(1003, "Clerk 1003"), new Clerk(1004, "Clerk 1004"), new Clerk(1005, "Clerk 1005")), clerks);
      Assertions.assertIterableEquals(Arrays.asList(new Clerk(1002, "Clerk 1002"), new Clerk(1005, "Clerk 1005")),
          managers);
    }
  }

  @Disabled("Not sure if there is a need for this usage.")
  @Test
  void testPartiallyImmutableObject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store6 store = mapper.getAStore6(1);
      List<Isle> isles = store.getIsles();
      Assertions.assertEquals("Store 1", store.getName());
      Assertions.assertEquals(3, isles.size());
    }
  }

  @Test
  void testTwoListsOfString() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store7 store = mapper.getAStore7(1);
      List<String> isleNames = store.getIsleNames();
      List<String> clerkNames = store.getClerkNames();
      Assertions.assertIterableEquals(Arrays.asList("Isle 101", "Isle 102", "Isle 103"), isleNames);
      Assertions.assertIterableEquals(
          Arrays.asList("Clerk 1001", "Clerk 1002", "Clerk 1003", "Clerk 1004", "Clerk 1005"), clerkNames);
    }
  }

  @Test
  void testCollectionArgWithTypeHandler() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store8 store = mapper.getAStore8(1);
      Assertions.assertIterableEquals(Arrays.asList("a", "b", "c"), store.getStrings());
    }
  }

  @Test
  @Disabled
  void testImmutableNestedObjects() {
    /*
     * This resultmap contains mixed property and constructor mappings, the logic assumes the entire chain will be
     * immutable when we have mixed mappings, we don't know when to create the final object, as property mappings could
     * still be modified at any point in time This brings us to a design question, is this really what we want from this
     * functionality, as the point was to create immutable objects in my opinion, supporting this defeats the purpose;
     * for example propery mapping -> immutable collection -> immutable object -> mapped by property mapping. we cannot
     * build the final object if it can still be modified; i.e, the signal to build the immutable object is lost. the
     * code in this pr assumes and relies on the base object also being immutable, i.e: constructor mapping -> immutable
     * collection -> immutable object -> mapped by constructor mapping. Imo, there is only one option here, it should be
     * added in the documentation; as doing (and supporting this, will be extremely complex)
     */
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Container container = mapper.getAContainer();
      Assertions
          .assertEquals(
              Arrays
                  .asList(
                      new Store(1, "Store 1",
                          Arrays.asList(new Isle(101, "Isle 101"), new Isle(102, "Isle 102"),
                              new Isle(103, "Isle 103"))),
                      new Store(2, "Store 2", Collections.emptyList()),
                      new Store(3, "Store 3", Arrays.asList(new Isle(104, "Isle 104"), new Isle(105, "Isle 105")))),
              container.getStores());
    }
  }
}
