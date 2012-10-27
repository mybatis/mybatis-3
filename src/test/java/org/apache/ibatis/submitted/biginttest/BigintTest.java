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
package org.apache.ibatis.submitted.biginttest;

import java.io.Reader;
import java.math.BigInteger;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BigintTest {

  private static final String SQL = "org/apache/ibatis/submitted/biginttest/CreateDB.sql";
  private static final String XML = "org/apache/ibatis/submitted/biginttest/mybatis-config.xml";

  private static SqlSessionFactory sqlSessionFactory;

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

  @Test
  public void shouldGetAUser() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      BigInteger key = new BigInteger("707070656505050302797979792923232303");

      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      Assert.assertEquals("User1", user.getName());
      Assert.assertEquals(key, user.getPublicKey());
    } finally {
      sqlSession.close();
    }
  }

}
