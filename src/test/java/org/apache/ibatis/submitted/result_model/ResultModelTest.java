/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.submitted.result_model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.ResultModel;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResultModelTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources
      .getResourceAsReader("org/apache/ibatis/submitted/result_model/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(SqlProviderMapper.class);
    }
    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/result_model/CreateDB.sql");
  }

  @Test
  void shouldGetSimpleModel() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SqlProviderMapper mapper = sqlSession.getMapper(SqlProviderMapper.class);
      final Item item = mapper.fetchOneItem();
      assertNotNull(item);
      assertEquals(1, item.getId());
      assertEquals("name", item.getName());
    }
  }

  @Test
  void shouldGetNestedAssociationModel() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SqlProviderMapper mapper = sqlSession.getMapper(SqlProviderMapper.class);
      final House house = mapper.fetchOneHouse();
      assertNotNull(house);
      assertEquals(1, house.getId());
      assertEquals("grandma home", house.getName());
      assertEquals(house.getOwnerId(), 1);
      assertNotNull(house.getPerson());
      assertEquals(1, house.getPerson().getId());
      assertEquals("grandma", house.getPerson().getName());
    }
  }

  @Test
  void shouldGetNestedCollectionModel() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SqlProviderMapper mapper = sqlSession.getMapper(SqlProviderMapper.class);
      final Person person = mapper.fetchOnePerson();
      assertNotNull(person);
      assertEquals(1, person.getId());
      assertEquals("name", person.getName());
      assertNotNull(person.getItems());
      assertEquals(person.getItems().size(), 1);
      final Item item = person.getItems().get(0);
      assertEquals(item.getId(), 2);
      assertEquals(item.getName(), "item_name");
    }
  }

  @Test
  void shouldGetMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SqlProviderMapper mapper = sqlSession.getMapper(SqlProviderMapper.class);
      final Map<String, Object> map = mapper.fetchOneMap();
      assertNotNull(map);
      assertEquals(1, map.get("ID"));
      assertEquals("name", map.get("NAME"));
    }
  }

  @Test
  void shouldGetList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SqlProviderMapper mapper = sqlSession.getMapper(SqlProviderMapper.class);
      final List<Object> list = mapper.fetchList();
      assertNotNull(list);
      assertEquals(3, list.size());
      assertEquals(1, list.get(0));
      assertEquals(2, list.get(1));
      assertEquals(3, list.get(2));
    }
  }

  @Test
  void shouldGetNestedMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SqlProviderMapper mapper = sqlSession.getMapper(SqlProviderMapper.class);
      final HouseWithPersonMap house = mapper.fetchOneHouseWithPersonMap();
      assertNotNull(house);
      assertEquals(1, house.getId());
      assertEquals("grandma home", house.getName());
      assertEquals(house.getOwnerId(), 1);
      assertNotNull(house.getPerson());
      assertEquals(1, house.getPerson().get("ID"));
      assertEquals("grandma", house.getPerson().get("NAME"));
    }
  }

  public interface SqlProviderMapper {

    @SelectProvider(type = SqlProvider.class, method = "fetchOneItem")
    @ResultModel(id = "fetchOneItem", type = Item.class)
    Item fetchOneItem();

    @SelectProvider(type = SqlProvider.class, method = "fetchOnePerson")
    @ResultModel(id = "fetchOnePerson", type = Person.class)
    Person fetchOnePerson();

    @SelectProvider(type = SqlProvider.class, method = "fetchOneHouse")
    @ResultModel(type = House.class)
    House fetchOneHouse();

    @SelectProvider(type = SqlProvider.class, method = "fetchOneMap")
    @ResultModel(id = "fetchOnePersonMap", type = HashMap.class)
    Map<String, Object> fetchOneMap();

    @SelectProvider(type = SqlProvider.class, method = "fetchList")
    @ResultModel(id = "fetchList", type = ArrayList.class)
    List<Object> fetchList();

    @SelectProvider(type = SqlProvider.class, method = "fetchOneHouse")
    @ResultModel(id = "fetchOneHouseWithPersonMap", type = HouseWithPersonMap.class)
    HouseWithPersonMap fetchOneHouseWithPersonMap();

    @SuppressWarnings("unused")
    class SqlProvider {
      public static String fetchOneItem() {
        return "SELECT 1 AS id, 'name' AS name FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static String fetchOnePerson() {
        return "SELECT 1 AS id, 'name' AS name, 2 AS item_id, 'item_name' AS item_name  FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static String fetchOneHouse() {
        return "" +
          "SELECT houses.*, persons.id AS person_id, persons.name AS person_name " +
          "FROM houses " +
          "JOIN persons ON houses.owner = persons.id " +
          "WHERE persons.id = 1";
      }

      public static String fetchOneMap() {
        return "SELECT 1 AS id, 'name' AS name FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static String fetchList() {
        return "SELECT id FROM persons";
      }
    }
  }

}
