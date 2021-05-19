/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.no_param_type;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NoParamTypeTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/no_param_type/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/no_param_type/CreateDB.sql");
  }

  @Test
  void shouldAcceptDifferentTypeInTheSameBatch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      ObjA a = new ObjA();
      a.setId(1);
      a.setName(111);
      sqlSession.insert("insertUser", a);
      ObjB b = new ObjB();
      b.setId(2);
      b.setName("222");
      sqlSession.insert("insertUser", b);
      List<BatchResult> batchResults = sqlSession.flushStatements();
      batchResults.clear();
      sqlSession.clearCache();
      sqlSession.commit();
      List<User> users = sqlSession.selectList("selectUser");
      assertEquals(2, users.size());
    }
  }

  public static class ObjA {
    private Integer id;

    private Integer name;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public Integer getName() {
      return name;
    }

    public void setName(Integer name) {
      this.name = name;
    }
  }

  public static class ObjB {
    private Integer id;

    private String name;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
