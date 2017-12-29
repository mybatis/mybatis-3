/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.mapper_type_parameter;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class MapperTypeParameterTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/mapper_type_parameter/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/mapper_type_parameter/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void shouldResolveReturnType() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      Person person = mapper.select(new Person(1));
      assertEquals("Jane", person.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveListTypeParam() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> persons = mapper.selectList(null);
      assertEquals(2, persons.size());
      assertEquals("Jane", persons.get(0).getName());
      assertEquals("John", persons.get(1).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveMultipleTypeParam() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
      Map<Long, Country> results = mapper.selectMap(new Country());
      assertEquals(2, results.size());
      assertEquals("Japan", results.get(1L).getName());
      assertEquals("New Zealand", results.get(2L).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveParameterizedReturnType() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonListMapper mapper = sqlSession.getMapper(PersonListMapper.class);
      List<Person> persons = mapper.select(null);
      assertEquals(2, persons.size());
      assertEquals("Jane", persons.get(0).getName());
      assertEquals("John", persons.get(1).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveParam() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
      assertEquals(1, mapper.update(new Country(2L, "Greenland")));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveListParam() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      Person person1 = new Person("James");
      assertEquals(1, mapper.insert(Arrays.asList(person1)));
      assertNotNull(person1.getId());
    } finally {
      sqlSession.close();
    }
  }
}
