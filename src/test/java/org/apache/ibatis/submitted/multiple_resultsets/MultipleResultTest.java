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
package org.apache.ibatis.submitted.multiple_resultsets;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

/*
 * This class contains tests for multiple results.  
 * It is based on Jeff's ref cursor tests.
 */
public class MultipleResultTest {

  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    //  Launch PostgreSQL server. Download / unarchive if necessary.
    postgres.start(EmbeddedPostgres.cachedRuntimeConfig(Paths.get("target/postgres")), "localhost", 5432, "multiple_resultsets", "postgres", "root", Collections.emptyList());

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multiple_resultsets/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multiple_resultsets/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @AfterClass
  public static void tearDown() {
    postgres.stop();
  }

  @Test
  public void shouldGetMultipleResultSetsWithOneStatement() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<?> usersAndGroups = mapper.getUsersAndGroups();
      Assert.assertEquals(2, usersAndGroups.size());
    } finally {
      sqlSession.close();
    }
  }

}
