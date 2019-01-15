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
package org.apache.ibatis.submitted.selectkey;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class SelectKeyTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  public void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/selectkey/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(AnnotatedMapper.class);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/selectkey/CreateDB.sql");
  }

  @Test
  public void testSelectKey() throws Exception {
    // this test checks to make sure that we can have select keys with the same
    // insert id in different namespaces
    String resource = "org/apache/ibatis/submitted/selectkey/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(reader);
    assertNotNull(sqlMapper);
  }

  @Test
  public void testInsertTable1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> parms = new HashMap<String, String>();
      parms.put("name", "Fred");
      int rows = sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table1.insert", parms);
      assertEquals(1, rows);
      assertEquals(11, parms.get("id"));
    }
  }

  @Test
  public void testInsertTable2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> parms = new HashMap<String, String>();
      parms.put("name", "Fred");
      int rows = sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insert", parms);
      assertEquals(1, rows);
      assertEquals(22, parms.get("id"));
    }
  }

  @Test
  public void testSeleckKeyReturnsNoData() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> parms = new HashMap<String, String>();
      parms.put("name", "Fred");
      Assertions.assertThrows(PersistenceException.class, () -> {
        sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertNoValuesInSelectKey", parms);
      });
    }
  }

  @Test
  public void testSeleckKeyReturnsTooManyData() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> parms = new HashMap<String, String>();
      parms.put("name", "Fred");
      sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertTooManyValuesInSelectKey", parms);
      Assertions.assertThrows(PersistenceException.class, () -> {
        sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertTooManyValuesInSelectKey", parms);
      });
    }
  }

  @Test
  public void testAnnotatedInsertTable2() {
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
  public void testAnnotatedInsertTable2WithGeneratedKey() {
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
  public void testAnnotatedUpdateTable2WithGeneratedKey() {
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
  public void testAnnotatedUpdateTable2WithGeneratedKeyXml() {
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
  public void testAnnotatedInsertTable2WithGeneratedKeyXml() {
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
  public void testAnnotatedInsertTable2WithSelectKeyWithKeyMap() {
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
  public void testAnnotatedUpdateTable2WithSelectKeyWithKeyMap() {
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
  public void testAnnotatedInsertTable2WithSelectKeyWithKeyMapXml() {
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
  public void testAnnotatedUpdateTable2WithSelectKeyWithKeyMapXml() {
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
  public void testAnnotatedInsertTable2WithSelectKeyWithKeyObject() {
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
  public void testAnnotatedUpdateTable2WithSelectKeyWithKeyObject() {
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
  public void testAnnotatedUpdateTable2WithSelectKeyWithKeyObjectXml() {
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
  public void testAnnotatedInsertTable2WithSelectKeyWithKeyObjectXml() {
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
  public void testAnnotatedInsertTable3() {
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
  public void testAnnotatedInsertTable3_2() {
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
  public void testSeleckKeyWithWrongKeyProperty() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("Kyoto");
      Assertions.assertThrows(PersistenceException.class, () -> {
        sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insertWrongKeyProperty", name);
      });
    }
  }

  @Test
  public void testUsingGeneratedKeysPolicyIsUse() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertUsingGeneratedKeysPolicyIsUse(name);
      assertEquals(1, rows);
      assertNotEquals(0, name.getNameId());
      assertEquals("barney_fred", name.getGeneratedName());
    }
  }

  @Test
  public void testUsingGeneratedKeysPolicyIsNotUse() throws IOException {
    SqlSessionFactory factory;
    try (Reader reader =
             Resources.getResourceAsReader("org/apache/ibatis/submitted/selectkey/MapperConfig.xml")) {
      factory = new SqlSessionFactoryBuilder().build(reader);
    }
    factory.getConfiguration().setUseGeneratedKeys(true);
    factory.getConfiguration().addMapper(AnnotatedMapper.class);

    try (SqlSession sqlSession = factory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertUsingGeneratedKeysPolicyIsNotUse(name);
      assertEquals(1, rows);
      assertEquals(0, name.getNameId());
      assertNull(name.getGeneratedName());
    }
  }

  @Test
  public void testUsingGeneratedKeysPolicyIsDefault() throws IOException {
    // test for default is false(=not use)
    {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        Name name = new Name();
        name.setName("barney");
        AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
        int rows = mapper.insertUsingGeneratedKeysPolicyIsDefault(name);
        assertEquals(1, rows);
        assertEquals(0, name.getNameId());
        assertNull(name.getGeneratedName());
      }
    }

    // test for default is true(=use)
    {
      SqlSessionFactory factory;
      try (Reader reader =
               Resources.getResourceAsReader("org/apache/ibatis/submitted/selectkey/MapperConfig.xml")) {
        factory = new SqlSessionFactoryBuilder().build(reader);
      }
      factory.getConfiguration().setUseGeneratedKeys(true);
      factory.getConfiguration().addMapper(AnnotatedMapper.class);

      try (SqlSession sqlSession = factory.openSession()) {
        Name name = new Name();
        name.setName("barney");
        AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
        int rows = mapper.insertUsingGeneratedKeysPolicyIsDefault(name);
        assertEquals(1, rows);
        assertNotEquals(0, name.getNameId());
        assertEquals("barney_fred", name.getGeneratedName());
      }
    }
  }

  @Test
  public void testSpecifyUseGeneratedKeysPolicyAndGeneratedKeysPolicy() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setName("barney");
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      int rows = mapper.insertSpecifyUseGeneratedKeysAndGeneratedKeysPolicy(name);
      assertEquals(1, rows);
      // ignore useGeneratedKeys option
      assertEquals(0, name.getNameId());
      assertNull(name.getGeneratedName());
    }
  }

}
