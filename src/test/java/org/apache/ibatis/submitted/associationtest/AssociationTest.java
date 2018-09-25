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
package org.apache.ibatis.submitted.associationtest;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AssociationTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/associationtest/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/associationtest/CreateDB.sql");
  }

  @Test
  public void shouldGetAllCars() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCars();
      Assert.assertEquals(4, cars.size());
      Assert.assertEquals("VW", cars.get(0).getType());
      Assert.assertNotNull(cars.get(0).getEngine());
      Assert.assertNull(cars.get(0).getBrakes());
      Assert.assertEquals("Opel", cars.get(1).getType());
      Assert.assertNull(cars.get(1).getEngine());
      Assert.assertNotNull(cars.get(1).getBrakes());
    }
  }

  @Test
  public void shouldGetOneCarWithOneEngineAndBrakes() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCars2();      
      Assert.assertEquals(1, cars.size());
      Assert.assertNotNull(cars.get(0).getEngine());
      Assert.assertNotNull(cars.get(0).getBrakes());      
    }
  }

  @Test
  public void shouldGetAllCarsNonUnique() {
    // this is a little weird - we might expect 4 objects back, but there are only
    // 1 distinct carid, so we get one back.
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Car> cars = mapper.getCars2();
      Assert.assertEquals(1, cars.size());
    }
  }

}
