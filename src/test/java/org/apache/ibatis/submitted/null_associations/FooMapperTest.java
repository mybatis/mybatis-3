/**
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
package org.apache.ibatis.submitted.null_associations;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FooMapperTest {

  private final static String SQL_MAP_CONFIG = "org/apache/ibatis/submitted/null_associations/sqlmap.xml";
  private static SqlSession session;
  private static Connection conn;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    final SqlSessionFactory factory = new SqlSessionFactoryBuilder()
        .build(Resources.getResourceAsReader(SQL_MAP_CONFIG));
    session = factory.openSession();
    conn = session.getConnection();

    BaseDataTest.runScript(factory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/null_associations/create-schema-mysql.sql");
  }

  @BeforeEach
  void setUp() {
    final FooMapper mapper = session.getMapper(FooMapper.class);
    mapper.deleteAllFoo();
    session.commit();
  }

  @Test
  void testNullAssociation() {
    final FooMapper mapper = session.getMapper(FooMapper.class);
    final Foo foo = new Foo(1L, null, true);
    mapper.insertFoo(foo);
    session.commit();
    final Foo read = mapper.selectFoo();
    Assertions.assertEquals(1L, read.getField1(), "Invalid mapping");
    Assertions.assertNull(read.getField2(), "Invalid mapping - field2 (Bar) should be null");
    Assertions.assertTrue(read.isField3(), "Invalid mapping");
  }

  @Test
  void testNotNullAssociation() {
    final FooMapper mapper = session.getMapper(FooMapper.class);
    final Bar bar = new Bar(1L, 2L, 3L);
    final Foo foo = new Foo(1L, bar, true);
    mapper.insertFoo(foo);
    session.commit();
    final Foo read = mapper.selectFoo();
    Assertions.assertEquals(1L, read.getField1(), "Invalid mapping");
    Assertions.assertNotNull(read.getField2(), "Bar should be not null");
    Assertions.assertTrue(read.isField3(), "Invalid mapping");
    Assertions.assertEquals(1L, read.getField2().getField1(), "Invalid mapping");
    Assertions.assertEquals(2L, read.getField2().getField2(), "Invalid mapping");
    Assertions.assertEquals(3L, read.getField2().getField3(), "Invalid mapping");
  }

  @AfterAll
  static void tearDownAfterClass() throws SQLException {
    conn.close();
    session.close();
  }

}
