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
