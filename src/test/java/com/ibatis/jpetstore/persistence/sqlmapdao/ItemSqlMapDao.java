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
import com.ibatis.jpetstore.domain.Item;
import com.ibatis.jpetstore.domain.LineItem;
import com.ibatis.jpetstore.domain.Order;
import com.ibatis.jpetstore.persistence.iface.ItemDao;

import java.util.HashMap;
import java.util.Map;

public class ItemSqlMapDao extends BaseSqlMapDao implements ItemDao {

  public ItemSqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  public void updateAllQuantitiesFromOrder(Order order) {
    for (int i = 0; i < order.getLineItems().size(); i++) {
      LineItem lineItem = (LineItem) order.getLineItems().get(i);
      String itemId = lineItem.getItemId();
      Integer increment = new Integer(lineItem.getQuantity());
      Map param = new HashMap(2);
      param.put("itemId", itemId);
      param.put("increment", increment);
      update("updateInventoryQuantity", param);
    }
  }

  public boolean isItemInStock(String itemId) {
    Integer i = (Integer) queryForObject("getInventoryQuantity", itemId);
    return (i != null && i.intValue() > 0);
  }

  public PaginatedList getItemListByProduct(String productId) {
    return queryForPaginatedList("getItemListByProduct", productId, PAGE_SIZE);
  }

  public Item getItem(String itemId) {
    Integer i = (Integer) queryForObject("getInventoryQuantity", itemId);
    Item item = (Item) queryForObject("getItem", itemId);
    item.setQuantity(i.intValue());
    return item;
  }

}
