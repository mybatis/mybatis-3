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

import com.testdomain.ComplexBean;

import java.util.HashMap;
import java.util.Map;

public class ComplexTypeTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/account-init.sql");
    initScript("com/scripts/order-init.sql");
    initScript("com/scripts/line_item-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  public void testMapBeanMap() throws Exception {
    Map map = new HashMap();
    ComplexBean bean = new ComplexBean();
    bean.setMap(new HashMap());
    bean.getMap().put("id", new Integer(1));
    map.put("bean", bean);

    Integer id = (Integer) sqlMap.queryForObject("mapBeanMap", map);

    assertEquals(id, bean.getMap().get("id"));
  }


}
