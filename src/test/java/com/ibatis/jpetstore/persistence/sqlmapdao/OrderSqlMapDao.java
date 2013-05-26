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
import com.ibatis.jpetstore.domain.LineItem;
import com.ibatis.jpetstore.domain.Order;
import com.ibatis.jpetstore.persistence.iface.OrderDao;

public class OrderSqlMapDao extends BaseSqlMapDao implements OrderDao {

  public OrderSqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  public PaginatedList getOrdersByUsername(String username) {
    return queryForPaginatedList("getOrdersByUsername", username, 10);
  }

  public Order getOrder(int orderId) {
    Order order = null;
    Object parameterObject = new Integer(orderId);
    order = (Order) queryForObject("getOrder", parameterObject);
    order.setLineItems(queryForList("getLineItemsByOrderId", new Integer(order.getOrderId())));
    return order;
  }

  public void insertOrder(Order order) {
    insert("insertOrder", order);
    insert("insertOrderStatus", order);
    for (int i = 0; i < order.getLineItems().size(); i++) {
      LineItem lineItem = (LineItem) order.getLineItems().get(i);
      lineItem.setOrderId(order.getOrderId());
      insert("insertLineItem", lineItem);
    }
  }

}
