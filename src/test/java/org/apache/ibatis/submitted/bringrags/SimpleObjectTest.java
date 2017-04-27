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
package org.apache.ibatis.submitted.bringrags;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleObjectTest {
  private SimpleChildObjectMapper simpleChildObjectMapper;
  private SqlSession sqlSession;

  @Before
  public void setUp() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/bringrags/mybatis-config.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    sqlSession = sqlSessionFactory.openSession();
    Connection conn = sqlSession.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(new StringReader("DROP TABLE IF EXISTS SimpleObject;"));
    runner.runScript(new StringReader("DROP TABLE IF EXISTS SimpleChildObject;"));
    runner.runScript(new StringReader("CREATE TABLE SimpleObject (id VARCHAR(5) NOT NULL);"));
    runner.runScript(new StringReader("CREATE TABLE SimpleChildObject (id VARCHAR(5) NOT NULL, simple_object_id VARCHAR(5) NOT NULL);"));
    runner.runScript(new StringReader("INSERT INTO SimpleObject (id) values ('10000');"));
    runner.runScript(new StringReader("INSERT INTO SimpleChildObject (id, simple_object_id) values ('20000', '10000');"));
    reader.close();
    simpleChildObjectMapper = (SimpleChildObjectMapper) sqlSession.getMapper(SimpleChildObjectMapper.class);
  }

  @After
  public void tearDown() throws Exception {
    sqlSession.close();
  }

  @Test
  public void testGetById() throws Exception {
    SimpleChildObject sc = simpleChildObjectMapper.getSimpleChildObjectById("20000");
    Assert.assertNotNull(sc);
    Assert.assertNotNull(sc.getSimpleObject());
  }

}