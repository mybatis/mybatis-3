/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.param_name_resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NoActualParamNameTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/param_name_resolve/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(Mapper.class);
      sqlSessionFactory.getConfiguration().setUseActualParamName(false);
    }

    // populate in-memory database
    try (Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/param_name_resolve/CreateDB.sql")) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.runScript(reader);
    }
  }

  @Test
  void testSingleListParameterWhenUseActualParamNameIsFalse() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      // use actual name -> no available and index parameter("0") is not available too
      {
        try {
          mapper.getUserCountUsingList(Arrays.asList(1, 2));
          fail();
        } catch (PersistenceException e) {
          assertEquals("Parameter 'ids' not found. Available parameters are [collection, list]",
              e.getCause().getMessage());
        }
      }
      // use 'collection' as alias
      {
        long count = mapper.getUserCountUsingListWithAliasIsCollection(Arrays.asList(1, 2));
        assertEquals(2, count);
      }
      // use 'list' as alias
      {
        long count = mapper.getUserCountUsingListWithAliasIsList(Arrays.asList(1, 2));
        assertEquals(2, count);
      }
    }
  }

  interface Mapper {
    @Select({
      "<script>",
      "  select count(*) from users u where u.id in",
      "  <foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>",
      "    #{item}",
      "  </foreach>",
      "</script>"
    })
    Long getUserCountUsingList(List<Integer> ids);

    @Select({
      "<script>",
      "  select count(*) from users u where u.id in",
      "  <foreach item='item' index='index' collection='collection' open='(' separator=',' close=')'>",
      "    #{item}",
      "  </foreach>",
      "</script>"
    })
    Long getUserCountUsingListWithAliasIsCollection(List<Integer> ids);

    @Select({
      "<script>",
      "  select count(*) from users u where u.id in",
      "  <foreach item='item' index='index' collection='list' open='(' separator=',' close=')'>",
      "    #{item}",
      "  </foreach>",
      "</script>"
    })
    Long getUserCountUsingListWithAliasIsList(List<Integer> ids);

  }

}
