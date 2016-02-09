/**
 *    Copyright 2009-2016 the original author or authors.
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
    reader.close();
    session.close();
  }

  @Test
  public void shouldResolveSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      Person person = mapper.select(1);
      assertEquals("Jane", person.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveSelectList() {
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
  public void shouldResolveMapKeyTypeParameter() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      Map<Integer, Person> persons = mapper.selectMap(null);
      assertEquals(2, persons.size());
      assertEquals("Jane", persons.get(1).getName());
      assertEquals("John", persons.get(2).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldResolveListTypeParameter() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonListMapper mapper = sqlSession.getMapper(PersonListMapper.class);
      List<Person> persons = mapper.select(null);
      assertEquals(2, persons.size());
      assertEquals("Jane", persons.get(0).getName());
    } finally {
      sqlSession.close();
    }
  }
}
