package com.ibatis.sqlmap;

import java.util.List;

public class PrivateMethodAccessTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/docs-init.sql");
  }

  public void testShouldSetPrivateProperties() throws Exception {
    List list = sqlMap.queryForList("getPrivateBooks");
    assertNotNull(list);
    assertEquals(2, list.size());
  }

}
