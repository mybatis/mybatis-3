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
package org.apache.ibatis.submitted.includes;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;

public class IncludeTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/includes/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/includes/CreateDB.sql");
  }

  @Test
  public void testIncludes()  {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final Integer result = sqlSession.selectOne("org.apache.ibatis.submitted.includes.mapper.selectWithProperty");
      Assert.assertEquals(Integer.valueOf(1), result);
    }
  }
  
  @Test
  public void testParametrizedIncludes() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final Map<String, Object> result = sqlSession.selectOne("org.apache.ibatis.submitted.includes.mapper.select");
      //Assert.assertEquals(Integer.valueOf(1), result);
    }
  }
  
}
