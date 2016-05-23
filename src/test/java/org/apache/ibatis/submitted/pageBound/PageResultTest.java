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
package org.apache.ibatis.submitted.pageBound;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageBounds;
import org.apache.ibatis.session.PageResult;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class PageResultTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/pageBound/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/pageBound/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

 
  
  @Test
  public void selectAll() {
	 
	  Map<String,Object> condition = new HashMap<String,Object>();
	  condition.put("sex", "male");
	  condition.put("minAge", Integer.valueOf(20));
	  condition.put("maxAge", Integer.valueOf(40));
	  PageBounds pageBounds = new PageBounds(1,3);
	  
	  SqlSession session = sqlSessionFactory.openSession();
	  PageResult<User> pageResult = session.selectPageBound("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll", condition, pageBounds);
	  session.commit();
	  session.close();
	  assertEquals(3, pageResult.getQueryList().size());
	  assertEquals(4, pageResult.getCount());
	  
	  
  }
  
  @Test
  public void selectAllMapper() {
	  
	  Map<String,Object> condition = new HashMap<String,Object>();
	  condition.put("sex", "male");
	  condition.put("minAge", Integer.valueOf(20));
	  condition.put("maxAge", Integer.valueOf(40));
	  PageBounds pageBounds = new PageBounds(1,3);
	  
	  SqlSession session = sqlSessionFactory.openSession();
	  UserMapper userMapper = session.getMapper(UserMapper.class);
	  PageResult<User> pageResult = userMapper.selectAll(condition, pageBounds);
	  session.commit();
	  session.close();
	  assertEquals(3, pageResult.getQueryList().size());
	  assertEquals(4, pageResult.getCount());
	  assertEquals(2, pageResult.getPageCount());
	  
	  System.out.println("count="+pageResult.getCount());
	  for(User u : pageResult.getQueryList())
	  {
		  System.out.println(u.getName());
	  }
  }
  
  @Test
  public void unionSelect() {
	 
	  Map<String,Object> condition = new HashMap<String,Object>();
	  condition.put("sex1", "male");
	  condition.put("sex2", "female");
	  PageBounds pageBounds = new PageBounds(1,3);
	  
	  SqlSession session = sqlSessionFactory.openSession();
	  UserMapper userMapper = session.getMapper(UserMapper.class);
	  PageResult<User> pageResult = userMapper.unionSelect(condition, pageBounds);
	  session.commit();
	  session.close();
	  assertEquals(3, pageResult.getQueryList().size());
	  assertEquals(6, pageResult.getCount());
	  assertEquals(2, pageResult.getPageCount());
	  
	  System.out.println("count="+pageResult.getCount());
	  for(User u : pageResult.getQueryList())
	  {
		  System.out.println(u.getName());
	  }
  }

  @Test
  public void cacheSelectAll() {
	 
	  	MappedStatement ms = sqlSessionFactory.getConfiguration().getMappedStatement("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll");
	    Cache cache = ms.getCache();
	    assertEquals("org.apache.ibatis.submitted.pageBound.UserMapper", cache.getId());
	    
	    assertEquals(1, cache.getSize());
  }

}
