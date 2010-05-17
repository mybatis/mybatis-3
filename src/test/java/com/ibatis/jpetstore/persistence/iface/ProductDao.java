package com.ibatis.jpetstore.persistence.iface;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.jpetstore.domain.Product;

public interface ProductDao {

  PaginatedList getProductListByCategory(String categoryId);

  Product getProduct(String productId);

  PaginatedList searchProductList(String keywords);

}
