/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.submitted.null_conversion_type_handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NullConversionTypeHanddlerTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/null_conversion_type_handler/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/null_conversion_type_handler/CreateDB.sql");
  }

  @Test
  void shouldConvertNullOnInsert() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      WorkerMapper mapper = sqlSession.getMapper(WorkerMapper.class);
      Worker worker = new Worker();
      worker.setId(2);
      worker.setName("Worker2");
      mapper.insert(worker);
      // verify
      assertEquals(99, mapper.selectBloodTypeValue(2));
    }
  }

  @Test
  void shouldConvertNullOnSelect() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      WorkerMapper mapper = sqlSession.getMapper(WorkerMapper.class);
      Worker worker = mapper.select(1);
      assertEquals(BloodType.UNKNOWN, worker.getBloodType());
    }
  }
}
