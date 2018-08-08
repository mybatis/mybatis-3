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
package org.apache.ibatis.submitted.primitive_result_type;

import org.apache.ibatis.BaseDataTest;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class PrimitiveResultTypeTest {

  @BeforeClass
  public static void setup() throws Exception {
    BaseDataTest.runScript(IbatisConfig.getSqlSessionFactory().getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/primitive_result_type/create.sql");
  }

  @Test
  public void shouldReturnProperPrimitiveType() {
    List<Integer> codes = ProductDAO.selectProductCodes();
    for (Object code : codes) {
      assertTrue(code instanceof Integer);
    }
    List<Long> lcodes = ProductDAO.selectProductCodesL();
    for (Object lcode : lcodes) {
      assertTrue(!(lcode instanceof Integer));
    }
    List<BigDecimal> bcodes = ProductDAO.selectProductCodesB();
    for (Object bcode : bcodes) {
      assertTrue(bcode instanceof BigDecimal);
    }
  }
  @Test
  public void noErrorThrowOut(){
      List<Product> products=ProductDAO.selectAllProducts();
      assertTrue("should return 4 results", 4==products.size());
  }
}
