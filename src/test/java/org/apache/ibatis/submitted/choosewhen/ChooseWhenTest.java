/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.choosewhen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChooseWhenTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/choosewhen/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().setLazyLoadingEnabled(true);
      sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/choosewhen/CreateDB.sql");
  }

  @Test
  public void shouldApplyOtherwiseWhenNoParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ChooseWhenMapper mapper = sqlSession.getMapper(ChooseWhenMapper.class);
      Map<String, Object> param = new HashMap<>(); // No name parameter
      List<User> users = mapper.selectUser(param);
      assertTrue(users.stream().allMatch(u -> "ACTIVE".equals(u.getStatus())));
    }
  }

}
