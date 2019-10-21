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
package org.apache.ibatis.submitted.local_cache;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocalCacheTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/local_cache/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/local_cache/CreateDB.sql");
  }

  @Test
  void useLocalCacheOnAnnotationMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      Random<Double> random1 = mapper.randWithUseLocalCache();
      Random<Double> random2 = mapper.randWithUseLocalCache();
      Assertions.assertEquals(random1.getValue(), random2.getValue());
      Assertions.assertSame(random1, random2);
    }
  }

  @Test
  void notUseLocalCacheOnAnnotationMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      Random<Double> random1 = mapper.randWithoutUseLocalCache();
      Random<Double> random2 = mapper.randWithoutUseLocalCache();
      Assertions.assertNotEquals(random1.getValue(), random2.getValue());
      Assertions.assertNotSame(random1, random2);
    }
  }

  @Test
  void useLocalCacheOnXmlMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);
      Random<Double> random1 = mapper.randWithUseLocalCache();
      Random<Double> random2 = mapper.randWithUseLocalCache();
      Assertions.assertEquals(random1.getValue(), random2.getValue());
      Assertions.assertSame(random1, random2);
    }
  }

  @Test
  void notUseLocalCacheOnXmlMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);
      Random<Double> random1 = mapper.randWithoutUseLocalCache();
      Random<Double> random2 = mapper.randWithoutUseLocalCache();
      Assertions.assertNotEquals(random1.getValue(), random2.getValue());
      Assertions.assertNotSame(random1, random2);
    }
  }

  @Test
  void mixed() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      Random<Double> cachedRandom1 = mapper.randWithUseLocalCache();
      Random<Double> random1 = mapper.randWithoutUseLocalCache();
      Random<Double> cachedRandom2 = mapper.randWithUseLocalCache();
      Random<Double> random2 = mapper.randWithoutUseLocalCache();

      Assertions.assertEquals(cachedRandom1.getValue(), cachedRandom2.getValue());
      Assertions.assertSame(cachedRandom1, cachedRandom2);
      Assertions.assertNotEquals(random1.getValue(), random2.getValue());
      Assertions.assertNotSame(random1, random2);
    }
  }

  @Test
  void nestedQueryWithUseLocalCache() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      XmlMapper mapper = sqlSession.getMapper(XmlMapper.class);
      Order order = mapper.selectOrder(1);
      Assertions.assertEquals(1, order.getId());
      Assertions.assertNotNull(order.getDateTime());
      Assertions.assertEquals(2, order.getLines().size());
      Assertions.assertEquals(1, order.getLines().get(0).getId());
      Assertions.assertEquals("Book", order.getLines().get(0).getName());
      Assertions.assertEquals(500, order.getLines().get(0).getUnitPrice());
      Assertions.assertEquals(2, order.getLines().get(0).getQuantity());
      Assertions.assertEquals(2, order.getLines().get(1).getId());
      Assertions.assertEquals("Drink", order.getLines().get(1).getName());
      Assertions.assertEquals(100, order.getLines().get(1).getUnitPrice());
      Assertions.assertEquals(10, order.getLines().get(1).getQuantity());
      Assertions.assertSame(order.getLines().get(0).getOrder(), order);
      Assertions.assertSame(order.getLines().get(1).getOrder(), order);
    }
  }

}
