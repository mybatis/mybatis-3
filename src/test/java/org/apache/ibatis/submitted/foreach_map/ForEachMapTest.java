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
package org.apache.ibatis.submitted.foreach_map;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ForEachMapTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUpClass() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/foreach_map/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/foreach_map/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Before
  public void setUp() throws Exception {
    sqlSession = sqlSessionFactory.openSession();
  }

  @After
  public void tearDown() throws Exception {
    sqlSession.close();
  }

  @Test
  public void shouldGetStringKeyStringValueEntries() {
    MapParam mapParam = new MapParam();
    mapParam.getMap().put("key 1", "value 1");
    mapParam.getMap().put("key 2", "value 2");
    sqlSession.insert("ins_string_string", mapParam);

    List<StringStringMapEntry> entries = sqlSession.selectList("sel_string_string", new MapParam());
    Assert.assertEquals(new StringStringMapEntry("key 1", "value 1"), entries.get(0));
    Assert.assertEquals(new StringStringMapEntry("key 2", "value 2"), entries.get(1));
  }

  @Test
  public void shouldGetIntKeyBoolValueEntries() throws Exception {
    MapParam mapParam = new MapParam();
    mapParam.getMap().put(12345, true);
    mapParam.getMap().put(54321, false);
    sqlSession.insert("ins_int_bool", mapParam);

    List<IntBoolMapEntry> entries = sqlSession.selectList("sel_int_bool");
    Assert.assertEquals(new IntBoolMapEntry(12345, true), entries.get(0));
    Assert.assertEquals(new IntBoolMapEntry(54321, false), entries.get(1));
  }

  @Test
  public void shouldGetNestedBeanKeyValueEntries() throws Exception {
    MapParam mapParam = new MapParam();
    mapParam.getMap().put(new NestedBean(12345, true), new NestedBean(54321, false));
    mapParam.getMap().put(new NestedBean(67890, true), new NestedBean(9876, false));
    sqlSession.insert("ins_nested_bean", mapParam);

    List<NestedBeanMapEntry> entries = sqlSession.selectList("sel_nested_bean");
    Assert.assertEquals(new NestedBeanMapEntry(12345, true, 54321, false), entries.get(0));
    Assert.assertEquals(new NestedBeanMapEntry(67890, true, 9876, false), entries.get(1));
  }
  
  @Test
  public void shouldSubstituteIndexWithKey() throws Exception {
    MapParam mapParam = new MapParam();
    mapParam.getMap().put("col_a", 22);
    mapParam.getMap().put("col_b", 222);
    Integer count = sqlSession.selectOne("sel_key_cols", mapParam);
    Assert.assertEquals(Integer.valueOf(1), count);
  }

  private SqlSession sqlSession;
}
