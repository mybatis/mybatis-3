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
package org.apache.ibatis.submitted.associationtest;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AssociationTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/associationtest/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/associationtest/CreateDB.sql");
  }

  @Test
  void shouldGetAllCars() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCars();
      Assertions.assertEquals(4, cars.size());
      Assertions.assertEquals("VW", cars.get(0).getType());
      Assertions.assertNotNull(cars.get(0).getEngine());
      Assertions.assertNull(cars.get(0).getBrakes());
      Assertions.assertEquals("Opel", cars.get(1).getType());
      Assertions.assertNull(cars.get(1).getEngine());
      Assertions.assertNotNull(cars.get(1).getBrakes());
    }
  }

  @Test
  void shouldGetOneCarWithOneEngineAndBrakes() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCars2();
      Assertions.assertEquals(1, cars.size());
      Assertions.assertNotNull(cars.get(0).getEngine());
      Assertions.assertNotNull(cars.get(0).getBrakes());
    }
  }

  @Test
  void shouldGetAllCarsNonUnique() {
    // this is a little weird - we might expect 4 objects back, but there are only
    // 1 distinct carid, so we get one back.
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCars2();
      Assertions.assertEquals(1, cars.size());
    }
  }

  @Test
  void shouldGetAllCarsAndDetectAssociationType() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCarsAndDetectAssociationType();
      Assertions.assertEquals(4, cars.size());
      Assertions.assertEquals("VW", cars.get(0).getType());
      Assertions.assertNotNull(cars.get(0).getEngine());
      Assertions.assertNull(cars.get(0).getBrakes());
      Assertions.assertEquals("Opel", cars.get(1).getType());
      Assertions.assertNull(cars.get(1).getEngine());
      Assertions.assertNotNull(cars.get(1).getBrakes());
    }
  }

}
