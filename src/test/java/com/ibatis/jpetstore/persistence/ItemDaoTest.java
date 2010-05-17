package com.ibatis.jpetstore.persistence;

import com.ibatis.jpetstore.domain.DomainFixture;
import com.ibatis.jpetstore.domain.Item;
import com.ibatis.jpetstore.domain.LineItem;
import com.ibatis.jpetstore.domain.Order;
import com.ibatis.jpetstore.persistence.iface.ItemDao;

public class ItemDaoTest extends BasePersistenceTest {

  private static final String MUTABLE_ITEM_ID = "EST-2";
  private static final String READ_ONLY_ITEM_ID = "EST-1";
  private static final String PRODUCT_ID = "FI-SW-01";

  private ItemDao itemDao = (ItemDao) daoMgr.getDao(ItemDao.class);

  public void testShouldFindItemByID() {
    assertNotNull(itemDao.getItem(READ_ONLY_ITEM_ID));
  }

  public void testShouldListTwoItemsForGivenProduct() {
    assertEquals(2, itemDao.getItemListByProduct(PRODUCT_ID).size());
  }

  public void testShouldVerifyItemIsInStock() {
    assertTrue("Expected item to be in stock.", itemDao.isItemInStock(READ_ONLY_ITEM_ID));
  }

  public void testShouldVerifyItemIsOutOfStock() {
    Order order = DomainFixture.newTestOrder();
    itemDao.updateAllQuantitiesFromOrder(order);
    assertFalse("Expected item to be out of stock.", itemDao.isItemInStock(MUTABLE_ITEM_ID));
  }

  public void testShouldUpdateInventoryForItem() {
    Item item = itemDao.getItem(MUTABLE_ITEM_ID);
    int inventory = item.getQuantity();
    Order order = DomainFixture.newTestOrder();
    inventory -= ((LineItem) order.getLineItems().get(0)).getQuantity();
    itemDao.updateAllQuantitiesFromOrder(order);
    item = itemDao.getItem(MUTABLE_ITEM_ID);
    assertEquals(inventory, item.getQuantity());
  }

}
