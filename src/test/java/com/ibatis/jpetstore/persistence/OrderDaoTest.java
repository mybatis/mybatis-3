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
package com.ibatis.jpetstore.persistence;

import com.ibatis.jpetstore.domain.DomainFixture;
import com.ibatis.jpetstore.domain.Order;
import com.ibatis.jpetstore.persistence.iface.OrderDao;

import java.math.BigDecimal;

public class OrderDaoTest extends BasePersistenceTest {

  private OrderDao orderDao = (OrderDao) daoMgr.getDao(OrderDao.class);

  private static final String USERNAME = "NewUsername";
  private static final String SEQUENCE_NAME = "ordernum";

  public void testShouldInsertNewOrderWithLineItems() {
    Order expected = DomainFixture.newTestOrder();
    int nextId = 900001;
    expected.setOrderId(nextId);
    orderDao.insertOrder(expected);
    Order actual = orderDao.getOrder(nextId);
    assertNotNull(actual);
    assertEquals(1, actual.getLineItems().size());
    assertEquals(new BigDecimal("99.99"), actual.getTotalPrice());
  }

  public void testShouldListASingleOrderForAUser() {
    Order expected = DomainFixture.newTestOrder();
    int nextId = 900002;
    expected.setOrderId(nextId);
    expected.setUsername(USERNAME);
    orderDao.insertOrder(expected);
    assertEquals(1, orderDao.getOrdersByUsername(USERNAME).size());
  }


}
