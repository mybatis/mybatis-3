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

import com.ibatis.common.resources.Resources;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiResultSetTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/DerbySqlMapConfig.xml", Resources.getResourceAsProperties("com/ibatis/sqlmap/maps/DerbySqlMapConfig.properties"));
    initScript("com/scripts/account-init.sql");
    initScript("com/scripts/derby-proc-init.sql");
  }

  public void testShouldRetrieveTwoSetsOfTwoAccountsFromMultipleResultMaps() throws Exception {
    Map persons = new HashMap();
    persons.put("1", new Integer(1));
    persons.put("2", new Integer(2));
    persons.put("3", new Integer(3));
    persons.put("4", new Integer(4));
    List results = sqlMap.queryForList("getMultiListsRm", persons);
    assertEquals(2, results.size());
    assertEquals(2, ((List) results.get(0)).size());
    assertEquals(2, ((List) results.get(1)).size());
  }

  public void testShouldRetrieveTwoSetsOfTwoAccountsFromMultipleResultClasses() throws Exception {
    Map persons = new HashMap();
    persons.put("1", new Integer(1));
    persons.put("2", new Integer(2));
    persons.put("3", new Integer(3));
    persons.put("4", new Integer(4));
    List results = sqlMap.queryForList("getMultiListsRc", persons);
    assertEquals(2, results.size());
    assertEquals(2, ((List) results.get(0)).size());
    assertEquals(2, ((List) results.get(1)).size());
  }

  public void testCallableStatementShouldReturnTwoResultSets() throws Exception {
    sqlMap.startTransaction();
    Connection conn = sqlMap.getCurrentConnection();
    CallableStatement cs = conn.prepareCall("{call MRESULTSET(?,?,?,?)}");
    cs.setInt(1, 1);
    cs.setInt(2, 2);
    cs.setInt(3, 3);
    cs.setInt(4, 4);
    cs.execute();
    ResultSet rs = cs.getResultSet();
    assertNotNull(rs);
    int found = 1;
    while (cs.getMoreResults()) {
      assertNotNull(cs.getResultSet());
      found++;
    }
    rs.close();
    cs.close();
    assertEquals("Didn't find second result set.", 2, found);
  }


}
