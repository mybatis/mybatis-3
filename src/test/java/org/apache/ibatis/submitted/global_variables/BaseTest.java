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
package org.apache.ibatis.submitted.global_variables;

import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.Connection;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void shouldGetAUser() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      CustomCache customCache = unwrap(sqlSessionFactory.getConfiguration().getCache(Mapper.class.getName()));
      Assert.assertEquals("User1", user.getName());
      Assert.assertEquals("foo", customCache.getStringValue());
      Assert.assertEquals(10, customCache.getIntegerValue().intValue());
      Assert.assertEquals(1000, customCache.getLongValue());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetAUserFromAnnotation() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      User user = mapper.getUser(1);
      CustomCache customCache = unwrap(sqlSessionFactory.getConfiguration().getCache(Mapper.class.getName()));
      Assert.assertEquals("User1", user.getName());
      Assert.assertEquals("foo", customCache.getStringValue());
      Assert.assertEquals(10, customCache.getIntegerValue().intValue());
      Assert.assertEquals(1000, customCache.getLongValue());
    } finally {
      sqlSession.close();
    }
  }

  private CustomCache unwrap(Cache cache){
    Field field;
    try {
      field = cache.getClass().getDeclaredField("delegate");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
    try {
      field.setAccessible(true);
      return (CustomCache)field.get(cache);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } finally {
      field.setAccessible(false);
    }
  }
  
}
