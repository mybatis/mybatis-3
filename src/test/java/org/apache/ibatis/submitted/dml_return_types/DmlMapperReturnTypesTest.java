/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.dml_return_types;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;

public class DmlMapperReturnTypesTest {

  private static final String SQL = "org/apache/ibatis/submitted/dml_return_types/CreateDB.sql";
  private static final String XML = "org/apache/ibatis/submitted/dml_return_types/mybatis-config.xml";

  private static SqlSessionFactory sqlSessionFactory;

  private SqlSession sqlSession;
  private Mapper mapper;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader(XML);
    try {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    } finally {
      reader.close();
    }

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Connection conn = session.getConnection();
      reader = Resources.getResourceAsReader(SQL);
      try {
        ScriptRunner runner = new ScriptRunner(conn);
        runner.setLogWriter(null);
        runner.runScript(reader);
      } finally {
        reader.close();
      }
    } finally {
      session.close();
    }
  }

  @Before
  public void openSession() {
    sqlSession = sqlSessionFactory.openSession();
    mapper = sqlSession.getMapper(Mapper.class);
  }

  @After
  public void closeSession() {
    sqlSession.close();
  }

  @Test
  public void updateShouldReturnVoid() {
      mapper.updateReturnsVoid(new User(1, "updateShouldReturnVoid"));
  }

  @Test
  public void shouldReturnPrimitiveInteger() {
    final int rows = mapper.updateReturnsPrimitiveInteger(new User(1, "shouldReturnPrimitiveInteger"));
    assertEquals(1, rows);
  }

  @Test
  public void shouldReturnInteger() {
    final Integer rows = mapper.updateReturnsInteger(new User(1, "shouldReturnInteger"));
    assertEquals(Integer.valueOf(1), rows);
  }

  @Test
  public void shouldReturnPrimitiveLong() {
    final long rows = mapper.updateReturnsPrimitiveLong(new User(1, "shouldReturnPrimitiveLong"));
    assertEquals(1L, rows);
  }

  @Test
  public void shouldReturnLong() {
    final Long rows = mapper.updateReturnsLong(new User(1, "shouldReturnLong"));
    assertEquals(Long.valueOf(1), rows);
  }

  @Test
  public void shouldReturnPrimitiveBoolean() {
    final boolean rows = mapper.updateReturnsPrimitiveBoolean(new User(1, "shouldReturnPrimitiveBoolean"));
    assertEquals(true, rows);
  }

  @Test
  public void shouldReturnBoolean() {
    final Boolean rows = mapper.updateReturnsBoolean(new User(1, "shouldReturnBoolean"));
    assertEquals(Boolean.TRUE, rows);
  }

}