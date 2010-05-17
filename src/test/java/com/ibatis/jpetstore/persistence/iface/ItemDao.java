package com.ibatis.jpetstore.persistence.iface;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.jpetstore.domain.Item;
import com.ibatis.jpetstore.domain.Order;

public interface ItemDao {

  void updateAllQuantitiesFromOrder(Order order);

  boolean isItemInStock(String itemId);

  PaginatedList getItemListByProduct(String productId);

  Item getItem(String itemId);

}
