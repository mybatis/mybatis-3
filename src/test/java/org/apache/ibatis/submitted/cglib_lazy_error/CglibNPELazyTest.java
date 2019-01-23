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

class CglibNPELazyTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cglib_lazy_error/ibatisConfigLazy.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().setLazyLoadingEnabled(true);
      sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
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
  void testGrandParent() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedParent = personMapper.selectById(2);
      Person expectedGrandParent = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      Assertions.assertNotNull(person, "Persons must not be null");
      final Person actualParent = person.getParent();
      final Person actualGrandParent = person.getParent().getParent();
      Assertions.assertEquals(expectedParent, actualParent);
      Assertions.assertEquals(expectedGrandParent, actualGrandParent);
    }
  }

  @Test
  void testAncestor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedAncestor = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      Assertions.assertNotNull(person, "Persons must not be null");
      final Person actualAncestor = person.getAncestor();
      Assertions.assertEquals(expectedAncestor, actualAncestor);
    }
  }

}
