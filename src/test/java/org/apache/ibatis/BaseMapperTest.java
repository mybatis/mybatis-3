/*
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
package org.apache.ibatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

public abstract class BaseMapperTest {
  protected SqlSessionFactory sqlSessionFactory;
  protected SqlSession sqlSession;

  @Before
  public final void setupSession() throws Exception {
    // create a SqlSessionFactory
    final Reader reader = asReader("./mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    sqlSession = sqlSessionFactory.openSession();
    final SqlSession session = sqlSession;
    final Connection conn = session.getConnection();
    final Reader dbReader = asReader("./CreateDB.sql");
    final ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(dbReader);
    dbReader.close();
  }

  @After
  public final void tearDownSession() {
    sqlSession.close();
  }

  private Reader asReader(final String resource) throws IOException {
    return Resources.getUrlAsReader(getClass().getResource(resource).toString());
  }
}
