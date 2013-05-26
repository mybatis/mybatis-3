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
package org.apache.ibatis.submitted.encoding;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncodingTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    // save charset
    Charset charset = Resources.getCharset();

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:encoding", "sa", "");

      // make sure that the SQL file has been saved in UTF-8!
      Resources.setCharset(Charset.forName("utf-8"));
      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/encoding/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/encoding/EncodingConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();

    } finally {
      // restore charset
      Resources.setCharset(charset);
      
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testEncoding1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      EncodingMapper mapper = sqlSession.getMapper(EncodingMapper.class);
      String answer = mapper.select1();
      assertEquals("Mara\u00f1\u00f3n", answer);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testEncoding2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      EncodingMapper mapper = sqlSession.getMapper(EncodingMapper.class);
      String answer = mapper.select2();
      assertEquals("Mara\u00f1\u00f3n", answer);
    } finally {
      sqlSession.close();
    }
  }
}
