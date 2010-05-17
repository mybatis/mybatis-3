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

  /**
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
