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
package org.apache.ibatis.submitted.usesjava8.keycolumn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

/**
 * @author Jeff Butler
 */
public class InsertTest {

  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    //  Launch PostgreSQL server. Download / unarchive if necessary.
    postgres.start(EmbeddedPostgres.cachedRuntimeConfig(Paths.get("target/postgres")), "localhost", 5432, "keycolumn", "postgres", "root", Collections.emptyList());

    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/usesjava8/keycolumn/MapperConfig.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/usesjava8/keycolumn/CreateDB.sql");
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
  public void testInsertAnnotated() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");

      int rows = mapper.insertNameAnnotated(name);

      assertNotNull(name.getId());
      assertEquals(1, rows);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testInsertMapped() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");

      int rows = mapper.insertNameMapped(name);

      assertNotNull(name.getId());
      assertEquals(1, rows);
    } finally {
      sqlSession.close();
    }
  }

  @Ignore // Not supported yet in PostgreSQL
  @Test
  public void testInsertMappedBatch() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    try {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");
      mapper.insertNameMapped(name);
      Name name2 = new Name();
      name2.setFirstName("Wilma");
      name2.setLastName("Flintstone");
      mapper.insertNameMapped(name2);
      List<BatchResult> batchResults = sqlSession.flushStatements();
      assertNotNull(name.getId());
      assertNotNull(name2.getId());
      assertEquals(1, batchResults.size());
    } finally {
      sqlSession.close();
    }
  }

}
