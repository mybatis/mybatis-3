/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.result_handler_type;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

class DefaultResultHandlerTypeTest {

  @Test
  void testSelectList() throws Exception {
    String xmlConfig = "org/apache/ibatis/submitted/result_handler_type/MapperConfig.xml";
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Person> list = sqlSession
          .selectList("org.apache.ibatis.submitted.result_handler_type.PersonMapper.doSelect");
      assertEquals(list.size(), 2);
      assertEquals("java.util.LinkedList", list.getClass().getCanonicalName());
    }
  }

  @Test
  void testSelectMap() throws Exception {
    String xmlConfig = "org/apache/ibatis/submitted/result_handler_type/MapperConfig.xml";
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<Integer, Person> map = sqlSession
          .selectMap("org.apache.ibatis.submitted.result_handler_type.PersonMapper.doSelect", "id");
      assertEquals(map.size(), 2);
      assertEquals("java.util.LinkedHashMap", map.getClass().getCanonicalName());
    }
  }

  @Test
  void testSelectMapAnnotation() throws Exception {
    String xmlConfig = "org/apache/ibatis/submitted/result_handler_type/MapperConfig.xml";
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      Map<Integer, Person> map = mapper.selectAsMap();
      assertEquals(map.size(), 2);
      assertEquals("java.util.LinkedHashMap", map.getClass().getCanonicalName());
    }
  }

  private SqlSessionFactory getSqlSessionFactoryXmlConfig(String resource) throws Exception {
    try (Reader configReader = Resources.getResourceAsReader(resource)) {
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
      BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
          "org/apache/ibatis/submitted/result_handler_type/CreateDB.sql");

      return sqlSessionFactory;
    }
  }

}
