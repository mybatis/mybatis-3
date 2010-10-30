package com.ibatis.sqlmap;

import com.testdomain.Category;
import com.testdomain.Product;

import java.util.List;

public class RepeatingGroupMappingTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/jpetstore-hsqldb-schema.sql");
    initScript("com/scripts/jpetstore-hsqldb-dataload.sql");
  }

  public void testShouldRetrieveFiveCategories() throws Exception {
    List categories = sqlMap.queryForList("getAllCategoriesMap");
    assertEquals(5,categories.size());
  }

  public void testGroupBy() throws Exception {
    List list = sqlMap.queryForList("getAllCategories", null);
    assertEquals(5, list.size());
  }

  public void testGroupByExtended() throws Exception {
    List list = sqlMap.queryForList("getAllCategoriesExtended", null);
    assertEquals(5, list.size());
  }

  public void testNestedProperties() throws Exception {
    List list = sqlMap.queryForList("getFish", null);
    assertEquals(1, list.size());

    Category cat = (Category) list.get(0);
    assertEquals("FISH", cat.getCategoryId());
    assertEquals("Fish", cat.getName());
    assertNotNull("Expected product list.", cat.getProductList());
    assertEquals(4, cat.getProductList().size());

    Product product = (Product) cat.getProductList().get(0);
    assertEquals(2, product.getItemList().size());
  }

  /**
   * This is a test case for iBATIS JIRA-250 "SELECT statement
   * returns unexpected result when 'groupBy' and 'nullValue'
   * are specified in resultMaps."
   * <p/>
   * The problem was that when a child object in a resultmap only
   * contained null values it would still be created when one of the
   * properties of the child object contained a nullValue attribute
   * in the ResultMap. The nullValue would be applied before checking
   * whether all properties of the child were 'null', so at least 1
   * property would always be non-null (the reason for the
   * nullValue attribute).
   * <p/>
   * The fix is to first check whether all properties of the child are
   * 'null', and if the child object contains at least 1 non-null property
   * to then only create the child object and apply the nullValue attribute.
   *
   * @throws Exception none should be thrown.
   */
  public void testGroupByJIRA250() throws Exception {
    List list = sqlMap.queryForList("getAllProductCategoriesJIRA250", null);
    Category cat = (Category) list.get(0);
    assertEquals(0, cat.getProductList().size());
  }
}
