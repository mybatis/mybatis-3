/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.valueinmap;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ValueInMapTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/valueinmap/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/valueinmap/CreateDB.sql");
  }

  @Test // issue #165
  public void shouldWorkWithAPropertyNamedValue() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, String> map = new HashMap<String, String>();
      map.put("table", "users");
      map.put("column", "name");
      map.put("value", "User1");
      Integer count = sqlSession.selectOne("count", map);
      Assert.assertEquals(new Integer(1), count);
    }
  }

  @Test(expected=PersistenceException.class)
  public void shouldWorkWithAList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<String> list = new ArrayList<String>();
      list.add("users");
      Integer count = sqlSession.selectOne("count2",list);
      Assert.assertEquals(new Integer(1), count);
    }
  }

}
