/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.sqlmap;

import com.testdomain.IItem;
import com.testdomain.ISupplier;

import java.util.List;

public class ResultObjectFactoryTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig_rof.xml", null);
    initScript("com/scripts/jpetstore-hsqldb-schema.sql");
    initScript("com/scripts/jpetstore-hsqldb-dataload.sql");
  }

  /*
   * This tests that the result object factory is working -
   * everything in the sql map is declared as an interface.
   */
  public void testShouldDemonstrateThatTheObjectFactoryIsWorking() throws Exception {
    List results = sqlMap.queryForList("getAllItemsROF");
    assertEquals(28, results.size());
    IItem iItem = (IItem) results.get(2);
    ISupplier iSupplier = iItem.getSupplier();
    Integer id = iSupplier.getSupplierId();
    assertEquals((Integer) 1, id);
  }

}
