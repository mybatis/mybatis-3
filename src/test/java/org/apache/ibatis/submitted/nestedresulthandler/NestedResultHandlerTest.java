/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.submitted.nestedresulthandler;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NestedResultHandlerTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedresulthandler/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedresulthandler/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void testGetPerson() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      List<Person> persons = mapper.getPersons();

      Person person = persons.get(0);
      Assert.assertEquals("grandma", person.getName());
      Assert.assertTrue(person.owns("book"));
      Assert.assertTrue(person.owns("tv"));
      Assert.assertEquals(2, person.getItems().size());

      person = persons.get(1);
      Assert.assertEquals("sister", person.getName());
      Assert.assertTrue(person.owns("phone"));
      Assert.assertTrue(person.owns("shoes"));
      Assert.assertEquals(2, person.getItems().size());

      person = persons.get(2);
      Assert.assertEquals("brother", person.getName());
      Assert.assertTrue(person.owns("car"));
      Assert.assertEquals(1, person.getItems().size());
    } finally {
      sqlSession.close();
    }
  }

  @Test // issue #542
  public void testGetPersonWithHandler() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      sqlSession.select("getPersons", new ResultHandler() {
        public void handleResult(ResultContext context) {
          Person person = (Person) context.getResultObject();
          if ("grandma".equals(person.getName())) {
            Assert.assertEquals(2, person.getItems().size());
          }
        }
      });
    } finally {
      sqlSession.close();
    }
  }

  /**
   * Fix bug caused by issue #542, see new issue #22 on github
   * If we order by a nested result map attribute we can miss some records and end up with duplicates instead.
   */
  @Test
  public void testGetPersonOrderedByItem() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      List<Person> persons = mapper.getPersonsWithItemsOrdered();

      Person person = persons.get(0);
      Assert.assertEquals("grandma", person.getName());
      Assert.assertTrue(person.owns("book"));
      Assert.assertTrue(person.owns("tv"));
      Assert.assertEquals(2, person.getItems().size());

      person = persons.get(1);
      Assert.assertEquals("brother", person.getName());
      Assert.assertTrue(person.owns("car"));
      Assert.assertEquals(1, person.getItems().size());

      person = persons.get(2);
      Assert.assertEquals("sister", person.getName());
      Assert.assertTrue(person.owns("phone"));
      Assert.assertTrue(person.owns("shoes"));
      Assert.assertEquals(2, person.getItems().size());
    } finally {
      sqlSession.close();
    }
  }

}
