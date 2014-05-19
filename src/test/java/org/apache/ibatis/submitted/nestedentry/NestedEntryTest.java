/*
 *    Copyright 2009-2013 the original author or authors.
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
package org.apache.ibatis.submitted.nestedentry;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class NestedEntryTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedentry/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    final SqlSession session = sqlSessionFactory.openSession();
    final Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedentry/CreateDB.sql");
    final ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

    /**
     * Test to reproduce https://github.com/mybatis/mybatis-3/issues/207
     */
    @Test
    public void testNestedMapEntries() {
        final Map<String, String> nestedParams = new HashMap<String, String>();
        nestedParams.put("A", "a");
        nestedParams.put("B", "b");
        nestedParams.put("C", "c");
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("nestedParams", nestedParams.entrySet());

        final SqlSession session = sqlSessionFactory.openSession(true);
        try {
            final Map<String, Object> result = session.selectOne(
                    "arrayFromValues", params);
            assertTrue(result.containsKey("data"));
        } finally {
            session.close();
        }
    }
    
}
