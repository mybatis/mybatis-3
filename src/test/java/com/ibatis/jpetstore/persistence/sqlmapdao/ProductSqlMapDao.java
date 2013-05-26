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
