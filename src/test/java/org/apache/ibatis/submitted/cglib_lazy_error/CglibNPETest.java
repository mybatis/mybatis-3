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
package org.apache.ibatis.submitted.cglib_lazy_error;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CglibNPETest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cglib_lazy_error/ibatisConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/cglib_lazy_error/CreateDB.sql");
  }

  @Test
  public void testNoParent() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.selectById(1);
      Assert.assertNotNull("Persons must not be null", person);
      Person parent = person.getParent();
      Assert.assertNull("Parent must be null", parent);
    }
  }

  @Test
  public void testAncestorSelf() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.selectById(1);
      Assert.assertNotNull("Persons must not be null", person);
      Person ancestor = person.getAncestor();
      Assert.assertEquals("Ancestor must be John Smith sr.", person, ancestor);
    }
  }

  @Test
  public void testGrandParent() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedParent = personMapper.selectById(2);
      Person expectedGrandParent = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      Assert.assertNotNull("Persons must not be null", person);
      Assert.assertEquals("Parent must be John Smith", expectedParent, person.getParent());
      Assert.assertEquals("Parent must be John Smith sr.", expectedGrandParent, person.getParent().getParent());
    }
  }

  @Test
  public void testAncestor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedAncestor = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      Assert.assertNotNull("Persons must not be null", person);
      Assert.assertEquals("Ancestor must be John Smith sr.", expectedAncestor, person.getAncestor());
    }
  }

  @Test
  public void testAncestorAfterQueryingParents() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person expectedAncestor = personMapper.selectById(1);
      Person person = personMapper.selectById(3);
      // Load ancestor indirectly.
      Assert.assertNotNull("Persons must not be null", person);
      Assert.assertNotNull("Parent must not be null", person.getParent());
      Assert.assertNotNull("Grandparent must not be null", person.getParent().getParent());
      Assert.assertEquals("Ancestor must be John Smith sr.", expectedAncestor, person.getAncestor());
    }
  }

  @Test
  public void testInsertBetweenTwoSelects() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person selected1 = personMapper.selectById(1);
      Person selected2 = personMapper.selectById(2);
      Person selected3 = personMapper.selectById(3);
      selected1.setId(4L);
      int rows = personMapper.insertPerson(selected1);
      Assert.assertEquals(1, rows);
      selected1 = personMapper.selectById(1);
      selected2 = personMapper.selectById(2);
      selected3 = personMapper.selectById(3);
      Person selected4 = personMapper.selectById(4);
      Assert.assertEquals(1, selected1.getId().longValue());
      Assert.assertEquals(2, selected2.getId().longValue());
      Assert.assertEquals(3, selected3.getId().longValue());
      Assert.assertEquals(4, selected4.getId().longValue());
    }
  }

  @Test
  public void testSelectWithStringSQLInjection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person selected1 = personMapper.selectByStringId("1");
      Assert.assertEquals(1, selected1.getId().longValue());
    }
  }

}
