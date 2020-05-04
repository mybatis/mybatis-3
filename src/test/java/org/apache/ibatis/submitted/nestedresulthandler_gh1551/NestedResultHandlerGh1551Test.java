/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.nestedresulthandler_gh1551;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NestedResultHandlerGh1551Test {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nestedresulthandler_gh1551/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/nestedresulthandler_gh1551/CreateDB.sql");
  }

  @Test
  void useColumnLabelIsTrue() {
    sqlSessionFactory.getConfiguration().setUseColumnLabel(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProductMapper mapper = sqlSession.getMapper(ProductMapper.class);

      ProductResp productResp = mapper.selectAllInfo("P001").get(0);

      Assertions.assertEquals("10000000000000000000000000000001", productResp.getId());
      Assertions.assertEquals("P001", productResp.getCode());
      Assertions.assertEquals("Product 001", productResp.getName());

      Assertions.assertEquals(1, productResp.getProductInfo().getId());
      Assertions.assertEquals("10000000000000000000000000000001", productResp.getProductInfo().getProductId());
      Assertions.assertEquals("Sale 50% Off", productResp.getProductInfo().getOtherInfo());

      Assertions.assertEquals("20000000000000000000000000000001", productResp.getSkus().get(0).getId());
      Assertions.assertEquals("10000000000000000000000000000001", productResp.getSkus().get(0).getProductId());
      Assertions.assertEquals("red", productResp.getSkus().get(0).getColor());
      Assertions.assertEquals("80", productResp.getSkus().get(0).getSize());
      Assertions.assertEquals("20000000000000000000000000000002", productResp.getSkus().get(1).getId());
      Assertions.assertEquals("10000000000000000000000000000001", productResp.getSkus().get(1).getProductId());
      Assertions.assertEquals("blue", productResp.getSkus().get(1).getColor());
      Assertions.assertEquals("10", productResp.getSkus().get(1).getSize());
    }
  }

  @Test
  void useColumnLabelIsFalse() {
    sqlSessionFactory.getConfiguration().setUseColumnLabel(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProductMapper mapper = sqlSession.getMapper(ProductMapper.class);
      PersistenceException exception = Assertions.assertThrows(PersistenceException.class, () -> mapper.selectAllInfo("P001"));
      Assertions.assertTrue(exception.getMessage().contains("Error attempting to get column 'ID' from result set.  Cause: java.sql.SQLSyntaxErrorException: incompatible data type in conversion: from SQL type VARCHAR to java.lang.Integer, value: 10000000000000000000000000000001"));
    }
  }

}
