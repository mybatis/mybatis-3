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
package org.apache.ibatis.submitted.simplelistparameter;

import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleListParameterTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/simplelistparameter/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/simplelistparameter/CreateDB.sql");
  }

  @Test
  public void shouldGetACar() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
      Car car = new Car();
      car.setDoors(Arrays.asList(new String[] {"2", "4"}));
      List<Car> cars = carMapper.getCar(car);
      Assert.assertNotNull(cars);
    }
  }

  @Test
  public void shouldResolveGenericFieldGetterType() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
      Rv rv = new Rv();
      rv.doors1 = Arrays.asList(new String[] {"2", "4"});
      List<Rv> rvs = carMapper.getRv1(rv);
      Assert.assertNotNull(rvs);
    }
  }

  @Test
  public void shouldResolveGenericMethodGetterType() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      CarMapper carMapper = sqlSession.getMapper(CarMapper.class);
      Rv rv = new Rv();
      rv.setDoors2(Arrays.asList(new String[] {"2", "4"}));
      List<Rv> rvs = carMapper.getRv2(rv);
      Assert.assertNotNull(rvs);
    }
  }
}
