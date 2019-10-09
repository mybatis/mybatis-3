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
package org.apache.ibatis.submitted.cglib_lazy_error;

import java.io.Reader;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CglibNPETest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cglib_lazy_error/ibatisConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/cglib_lazy_error/CreateDB.sql");
  }

  @Test
  void testNoParent() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.selectById(1);
      Assertions.assertNotNull(person, "Persons must not be null");
      Person parent = person.getParent();
      Assertions.assertNull(parent, "Parent must be null");
    }
  }

  @Test
  void testAncestorSelf() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.selectById(1);
      Assertions.assertNotNull(person, "Persons must not be null");
      Person ancestor = person.getAncestor();
      Assertions.assertEquals(person, ancestor, "Ancestor must be John Smith sr.");
    }
  }

  @Test
  void testGrandParent() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedParent = personMapper.selectById(2);
      Person expectedGrandParent = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      Assertions.assertNotNull(person, "Persons must not be null");
      Assertions.assertEquals(expectedParent, person.getParent(), "Parent must be John Smith");
      Assertions.assertEquals(expectedGrandParent, person.getParent().getParent(), "Parent must be John Smith sr.");
    }
  }

  @Test
  void testAncestor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedAncestor = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      Assertions.assertNotNull(person, "Persons must not be null");
      Assertions.assertEquals(expectedAncestor, person.getAncestor(), "Ancestor must be John Smith sr.");
    }
  }

  @Test
  void testAncestorAfterQueryingParents() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedAncestor = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      // Load ancestor indirectly.
      Assertions.assertNotNull(person, "Persons must not be null");
      Assertions.assertNotNull(person.getParent(), "Parent must not be null");
      Assertions.assertNotNull(person.getParent().getParent(), "Grandparent must not be null");
      Assertions.assertEquals(expectedAncestor, person.getAncestor(), "Ancestor must be John Smith sr.");
    }
  }

  @Test
  void testInsertBetweenTwoSelects() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person selected1 = personMapper.selectById(1);
      Person selected2 = personMapper.selectById(2);
      Person selected3 = personMapper.selectById(3);
      selected1.setId(4L);
      int rows = personMapper.insertPerson(selected1);
      Assertions.assertEquals(1, rows);
      selected1 = personMapper.selectById(1);
      selected2 = personMapper.selectById(2);
      selected3 = personMapper.selectById(3);
      Person selected4 = personMapper.selectById(4);
      Assertions.assertEquals(1, selected1.getId().longValue());
      Assertions.assertEquals(2, selected2.getId().longValue());
      Assertions.assertEquals(3, selected3.getId().longValue());
      Assertions.assertEquals(4, selected4.getId().longValue());
    }
  }

  @Test
  void testSelectWithStringSQLInjection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person selected1 = personMapper.selectByStringId("1");
      Assertions.assertEquals(1, selected1.getId().longValue());
    }
  }

}
