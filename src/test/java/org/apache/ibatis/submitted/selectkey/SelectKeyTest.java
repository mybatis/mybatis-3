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
package org.apache.ibatis.submitted.selectkey;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SelectKeyTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/selectkey/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(AnnotatedMapper.class);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/selectkey/CreateDB.sql");
  }

  @Test
  void testSelectKey() throws Exception {
    // this test checks to make sure that we can have select keys with the same
    // insert id in different namespaces
    String resource = "org/apache/ibatis/submitted/selectkey/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(reader);
    assertNotNull(sqlMapper);
  }

  @Test
  void testInsertTable1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, Object> parms = new HashMap<>();
      parms.put("name", "Fred");
      int rows = sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table1.insert", parms);
      assertEquals(1, rows);
      assertEquals(11, parms.get("id"));
    }
  }

  @Test
  void testInsertTable2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, Object> parms = new HashMap<>();
      parms.put("name", "Fred");
      int rows = sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insert", parms);
      assertEquals(1, rows);
      assertEquals(22, parms.get("id"));
    }
  }

  @Test
  void testSeleckKeyReturnsNoData() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> parms = new HashMap<>();
      parms.put("name", "Fred");
      Assertions.assertThrows(PersistenceException.class,
          () -> sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertNoValuesInSelectKey", parms));
    }
  }

  @Test
  void testSeleckKeyReturnsTooManyData() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> parms = new HashMap<>();
      parms.put("name", "Fred");
      sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertTooManyValuesInSelectKey", parms);
      Assertions.assertThrows(PersistenceException.class, () -> sqlSession
          .insert("org.apache.ibatis.submitted.selectkey.Table2.insertTooManyValuesInSelectKey", parms));
    }
  }

  @Test
  void testAnnotatedInsertTable2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
    }
  }

  @Test
  void testAnnotatedInsertTable2WithGeneratedKey() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithGeneratedKey(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  @Disabled("HSQLDB is not returning the generated column after the update")
  void testAnnotatedUpdateTable2WithGeneratedKey() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithGeneratedKey(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());

      name.setName("Wilma");
      rows = mapper.updateTable2WithGeneratedKey(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("Wilma_fred", name.getGeneratedName());
    }
  }

  @Test
  @Disabled("HSQLDB is not returning the generated column after the update")
  void testAnnotatedUpdateTable2WithGeneratedKeyXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithGeneratedKeyXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());

      name.setName("Wilma");
      rows = mapper.updateTable2WithGeneratedKeyXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("Wilma_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedInsertTable2WithGeneratedKeyXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithGeneratedKeyXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedInsertTable2WithSelectKeyWithKeyMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyMap(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedUpdateTable2WithSelectKeyWithKeyMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyMap(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());

      name.setName("Wilma");
      rows = mapper.updateTable2WithSelectKeyWithKeyMap(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("Wilma_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedInsertTable2WithSelectKeyWithKeyMapXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyMapXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedUpdateTable2WithSelectKeyWithKeyMapXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyMapXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());

      name.setName("Wilma");
      rows = mapper.updateTable2WithSelectKeyWithKeyMapXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("Wilma_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedInsertTable2WithSelectKeyWithKeyObject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyObject(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedUpdateTable2WithSelectKeyWithKeyObject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyObject(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());

      name.setName("Wilma");
      rows = mapper.updateTable2WithSelectKeyWithKeyObject(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("Wilma_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedUpdateTable2WithSelectKeyWithKeyObjectXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyObjectXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());

      name.setName("Wilma");
      rows = mapper.updateTable2WithSelectKeyWithKeyObjectXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("Wilma_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedInsertTable2WithSelectKeyWithKeyObjectXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable2WithSelectKeyWithKeyObjectXml(name);
      assertEquals(1, rows);
      assertEquals(22, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  void testAnnotatedInsertTable3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable3(name);
      assertEquals(1, rows);
      assertEquals(33, name.getNameId());
    }
  }

  @Test
  void testAnnotatedInsertTable3_2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertTable3_2(name);
      assertEquals(1, rows);
      assertEquals(33, name.getNameId());
    }
  }

  @Test
  void testSeleckKeyWithWrongKeyProperty() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("Kyoto");
      Assertions.assertThrows(PersistenceException.class,
          () -> sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertWrongKeyProperty", name));
    }
  }
}
