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
package org.apache.ibatis.submitted.nestedresulthandler;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NestedResultHandlerTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedresulthandler/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/nestedresulthandler/CreateDB.sql");
  }

  @Test
  void testGetPerson() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      List<Person> persons = mapper.getPersons();

      Person person = persons.get(0);
      Assertions.assertEquals("grandma", person.getName());
      Assertions.assertTrue(person.owns("book"));
      Assertions.assertTrue(person.owns("tv"));
      Assertions.assertEquals(2, person.getItems().size());

      person = persons.get(1);
      Assertions.assertEquals("sister", person.getName());
      Assertions.assertTrue(person.owns("phone"));
      Assertions.assertTrue(person.owns("shoes"));
      Assertions.assertEquals(2, person.getItems().size());

      person = persons.get(2);
      Assertions.assertEquals("brother", person.getName());
      Assertions.assertTrue(person.owns("car"));
      Assertions.assertEquals(1, person.getItems().size());
    }
  }

  @Test
  // issue #542
  void testGetPersonWithHandler() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      sqlSession.select("getPersons", context -> {
        Person person = (Person) context.getResultObject();
        if ("grandma".equals(person.getName())) {
          Assertions.assertEquals(2, person.getItems().size());
        }
      });
    }
  }

  @Test
  void testUnorderedGetPersonWithHandler() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Assertions.assertThrows(PersistenceException.class, () -> sqlSession.select("getPersonsWithItemsOrdered", context -> {
        Person person = (Person) context.getResultObject();
        if ("grandma".equals(person.getName())) {
          person.getItems().size();
        }
      }));
    }
  }

  /**
   * Fix bug caused by issue #542, see new issue #22 on github If we order by a
   * nested result map attribute we can miss some records and end up with
   * duplicates instead.
   */
  @Test
  void testGetPersonOrderedByItem() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      List<Person> persons = mapper.getPersonsWithItemsOrdered();

      Person person = persons.get(0);
      Assertions.assertEquals("grandma", person.getName());
      Assertions.assertTrue(person.owns("book"));
      Assertions.assertTrue(person.owns("tv"));
      Assertions.assertEquals(2, person.getItems().size());

      person = persons.get(1);
      Assertions.assertEquals("brother", person.getName());
      Assertions.assertTrue(person.owns("car"));
      Assertions.assertEquals(1, person.getItems().size());

      person = persons.get(2);
      Assertions.assertEquals("sister", person.getName());
      Assertions.assertTrue(person.owns("phone"));
      Assertions.assertTrue(person.owns("shoes"));
      Assertions.assertEquals(2, person.getItems().size());
    }
  }

  @Test // reopen issue 39? (not a bug?)
  void testGetPersonItemPairs() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<PersonItemPair> pairs = mapper.getPersonItemPairs();

      Assertions.assertNotNull(pairs);
      // System.out.println( new StringBuilder().append("selected pairs: ").append(pairs) );

      Assertions.assertEquals(5, pairs.size());
      Assertions.assertNotNull(pairs.get(0).getPerson());
      Assertions.assertEquals(pairs.get(0).getPerson().getId(), Integer.valueOf(1));
      Assertions.assertNotNull(pairs.get(0).getItem());
      Assertions.assertEquals(pairs.get(0).getItem().getId(), Integer.valueOf(1));
    }
  }

}
