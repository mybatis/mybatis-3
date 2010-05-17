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
