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
package org.apache.ibatis.submitted.collection_in_constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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
      List<Aisle> aisles = store.getAisles();
      Assertions.assertIterableEquals(
          Arrays.asList(new Aisle(101, "Aisle 101"), new Aisle(102, "Aisle 102"), new Aisle(103, "Aisle 103")), aisles);
    }
  }

  @Test
  void testSimpleList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Store> stores = mapper.getStores();
      Assertions.assertIterableEquals(
          Arrays.asList(new Aisle(101, "Aisle 101"), new Aisle(102, "Aisle 102"), new Aisle(103, "Aisle 103")),
          stores.get(0).getAisles());
      Assertions.assertTrue(stores.get(1).getAisles().isEmpty());
      Assertions.assertIterableEquals(Arrays.asList(new Aisle(104, "Aisle 104"), new Aisle(105, "Aisle 105")),
          stores.get(2).getAisles());
    }
  }

  @Test
  void shouldEmptyListBeReturned() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertTrue(mapper.getAStore(2).getAisles().isEmpty());
    }
  }

  @Test
  void testTwoLists() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store2 store = mapper.getAStore2(1);
      List<Clerk> clerks = store.getClerks();
      List<Aisle> aisles = store.getAisles();
      Assertions.assertIterableEquals(Arrays.asList(new Clerk(1001, "Clerk 1001"), new Clerk(1002, "Clerk 1002"),
          new Clerk(1003, "Clerk 1003"), new Clerk(1004, "Clerk 1004"), new Clerk(1005, "Clerk 1005")), clerks);
      Assertions.assertIterableEquals(
          Arrays.asList(new Aisle(101, "Aisle 101"), new Aisle(102, "Aisle 102"), new Aisle(103, "Aisle 103")), aisles);
    }
  }

  @Test
  void testListOfStrings() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store3 store = mapper.getAStore3(1);
      List<String> aisleNames = store.getAisleNames();
      Assertions.assertEquals(3, aisleNames.size());
      Assertions.assertIterableEquals(Arrays.asList("Aisle 101", "Aisle 102", "Aisle 103"), aisleNames);
    }
  }

  @Test
  void testObjectWithBuilder() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store4 store = mapper.getAStore4(1);
      List<Aisle> aisles = store.getAisles();
      Assertions.assertIterableEquals(
          Arrays.asList(new Aisle(101, "Aisle 101"), new Aisle(102, "Aisle 102"), new Aisle(103, "Aisle 103")), aisles);
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
      List<Aisle> aisles = store.getAisles();
      Assertions.assertEquals("Store 1", store.getName());
      Assertions.assertEquals(3, aisles.size());
    }
  }

  @Test
  void testTwoListsOfString() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Store7 store = mapper.getAStore7(1);
      List<String> aisleNames = store.getAisleNames();
      List<String> clerkNames = store.getClerkNames();
      Assertions.assertIterableEquals(Arrays.asList("Aisle 101", "Aisle 102", "Aisle 103"), aisleNames);
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
  void testCollectionArgWithNestedAndTypeHandler() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Store10> stores10 = mapper.getStores10();

      assertThat(stores10).isNotNull().hasSize(3)
          .extracting(Store10::getId, Store10::getName, store -> store.getClerks().size(), Store10::getStrings)
          .containsExactly(tuple(1, "Store 1", 5, List.of("a", "b", "c", "1")),
              tuple(2, "Store 2", 0, List.of("a", "b", "c", "2")), tuple(3, "Store 3", 0, List.of("a", "b", "c", "3")));

      assertThat(stores10.get(0).getClerks()).extracting(Clerk::getId, Clerk::getName).containsExactly(
          tuple(1001, "Clerk 1001"), tuple(1002, "Clerk 1002"), tuple(1003, "Clerk 1003"), tuple(1004, "Clerk 1004"),
          tuple(1005, "Clerk 1005"));
    }
  }

  @Test
  void testImmutableNestedObjects() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Container container = mapper.getAContainer();
      Assertions
          .assertEquals(
              Arrays.asList(
                  new Store(1, "Store 1",
                      Arrays.asList(new Aisle(101, "Aisle 101"), new Aisle(102, "Aisle 102"),
                          new Aisle(103, "Aisle 103"))),
                  new Store(2, "Store 2", Collections.emptyList()),
                  new Store(3, "Store 3", Arrays.asList(new Aisle(104, "Aisle 104"), new Aisle(105, "Aisle 105")))),
              container.getStores());
    }
  }

  @Test
  void testImmutableNestedObjectsWithBadEquals() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Container1> containers = mapper.getContainers();

      Container1 expectedContainer1 = new Container1();
      expectedContainer1.setNum(1);
      expectedContainer1.setType("storesWithClerks");
      expectedContainer1.setStores(Arrays.asList(
          new Store9(1, "Store 1",
              Arrays.asList(new Clerk(1001, "Clerk 1001"), new Clerk(1003, "Clerk 1003"),
                  new Clerk(1004, "Clerk 1004"))),
          new Store9(2, "Store 2", Arrays.asList()), new Store9(3, "Store 3", Arrays.asList())));

      Container1 expectedContainer2 = new Container1();
      expectedContainer2.setNum(1);
      expectedContainer2.setType("storesWithManagers");
      expectedContainer2.setStores(Arrays.asList(
          new Store9(1, "Store 1", Arrays.asList(new Clerk(1002, "Clerk 1002"), new Clerk(1005, "Clerk 1005")))));

      // cannot use direct equals as we overwrote it with a bad impl on purpose
      assertThat(containers).isNotNull().hasSize(2);
      assertContainer1(containers.get(0), expectedContainer1);
      assertContainer1(containers.get(1), expectedContainer2);
    }
  }

  private static void assertContainer1(Container1 container1, Container1 expectedContainer1) {
    assertThat(container1).isNotNull().satisfies(c -> {
      assertThat(c.getNum()).isEqualTo(expectedContainer1.getNum());
      assertThat(c.getType()).isEqualTo(expectedContainer1.getType());
      assertThat(c.getStores()).isEqualTo(expectedContainer1.getStores());
    });
  }
}
