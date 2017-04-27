/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.rounding;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RoundingHandlersTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/rounding/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/rounding/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void shouldGetAUser() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      Assert.assertEquals("User1", user.getName());
      Assert.assertEquals(RoundingMode.UP, user.getRoundingMode());
      user = mapper.getUser2(1);
      Assert.assertEquals("User1", user.getName());
      Assert.assertEquals(RoundingMode.UP, user.getRoundingMode());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertUser2() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      User user = new User();
      user.setId(2);
      user.setName("User2");
      user.setFunkyNumber(BigDecimal.ZERO);
      user.setRoundingMode(RoundingMode.UNNECESSARY);
      mapper.insert(user);
      mapper.insert2(user);
    } finally {
      session.close();
    }
  }

}
