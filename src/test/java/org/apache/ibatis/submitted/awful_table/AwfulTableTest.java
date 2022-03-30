/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.awful_table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AwfulTableTest {
  protected SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/awful_table/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/awful_table/CreateDB.sql");
  }

  @Test
  void testAwfulTableInsert() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AwfulTableMapper mapper = sqlSession.getMapper(AwfulTableMapper.class);
      AwfulTable record = new AwfulTable();
      record.seteMail("fred@fred.com");
      record.setEmailaddress("alsofred@fred.com");
      record.setFirstFirstName("fred1");
      record.setFrom("from field");
      record.setId1(1);
      record.setId2(2);
      record.setId5(5);
      record.setId6(6);
      record.setId7(7);
      record.setSecondFirstName("fred2");
      record.setThirdFirstName("fred3");

      mapper.insert(record);
      Integer generatedCustomerId = record.getCustomerId();
      assertEquals(57, generatedCustomerId.intValue());

      AwfulTable returnedRecord = mapper.selectByPrimaryKey(generatedCustomerId);

      assertEquals(generatedCustomerId, returnedRecord.getCustomerId());
      assertEquals(record.geteMail(), returnedRecord.geteMail());
      assertEquals(record.getEmailaddress(), returnedRecord.getEmailaddress());
      assertEquals(record.getFirstFirstName(), returnedRecord.getFirstFirstName());
      assertEquals(record.getFrom(), returnedRecord.getFrom());
      assertEquals(record.getId1(), returnedRecord.getId1());
      assertEquals(record.getId2(), returnedRecord.getId2());
      assertEquals(record.getId5(), returnedRecord.getId5());
      assertEquals(record.getId6(), returnedRecord.getId6());
      assertEquals(record.getId7(), returnedRecord.getId7());
      assertEquals(record.getSecondFirstName(), returnedRecord.getSecondFirstName());
      assertEquals(record.getThirdFirstName(), returnedRecord.getThirdFirstName());
    }
  }

}
