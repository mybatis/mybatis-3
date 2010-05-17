package com.ibatis.jpetstore.persistence;

import com.ibatis.jpetstore.persistence.iface.ProductDao;

public class ProductDaoTest extends BasePersistenceTest {

  private ProductDao productDao = (ProductDao) daoMgr.getDao(ProductDao.class);

  public void testShouldFindSpecificProductByID() {
    assertNotNull(productDao.getProduct("FI-SW-01"));
  }

  public void testShouldListProductsForACategory() {
    assertEquals(2, productDao.getProductListByCategory("CATS").size());
  }

  public void testShouldFindAllProductsContainingKeyword() {
    assertEquals(4, productDao.searchProductList("dog").size());
  }

}
