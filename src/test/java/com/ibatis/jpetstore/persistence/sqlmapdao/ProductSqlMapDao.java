package com.ibatis.jpetstore.persistence.sqlmapdao;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.jpetstore.domain.Product;
import com.ibatis.jpetstore.persistence.iface.ProductDao;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ProductSqlMapDao extends BaseSqlMapDao implements ProductDao {

  public ProductSqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  public PaginatedList getProductListByCategory(String categoryId) {
    return queryForPaginatedList("getProductListByCategory", categoryId, PAGE_SIZE);
  }

  public Product getProduct(String productId) {
    return (Product) queryForObject("getProduct", productId);
  }

  public PaginatedList searchProductList(String keywords) {
    Object parameterObject = new ProductSearch(keywords);
    return queryForPaginatedList("searchProductList", parameterObject, PAGE_SIZE);
  }

  /* Inner Classes */

  public static class ProductSearch {
    private List keywordList = new ArrayList();

    public ProductSearch(String keywords) {
      StringTokenizer splitter = new StringTokenizer(keywords, " ", false);
      while (splitter.hasMoreTokens()) {
        keywordList.add("%" + splitter.nextToken() + "%");
      }
    }

    public List getKeywordList() {
      return keywordList;
    }
  }

}
