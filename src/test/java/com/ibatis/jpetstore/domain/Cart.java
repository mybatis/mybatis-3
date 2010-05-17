package com.ibatis.jpetstore.domain;

import com.ibatis.common.util.PaginatedArrayList;
import com.ibatis.common.util.PaginatedList;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class Cart implements Serializable {

  private final Map itemMap = Collections.synchronizedMap(new HashMap());
  private final PaginatedList itemList = new PaginatedArrayList(4);

  public Iterator getCartItems() {
    return itemList.iterator();
  }

  public PaginatedList getCartItemList() {
    return itemList;
  }

  public int getNumberOfItems() {
    return itemList.size();
  }

  public Iterator getAllCartItems() {
    List allItems = new ArrayList();
    itemList.gotoPage(0);
    allItems.addAll(itemList);
    while (itemList.nextPage()) {
      allItems.addAll(itemList);
    }
    return allItems.iterator();
  }

  public boolean containsItemId(String itemId) {
    return itemMap.containsKey(itemId);
  }

  public void addItem(Item item, boolean isInStock) {
    CartItem cartItem = (CartItem) itemMap.get(item.getItemId());
    if (cartItem == null) {
      cartItem = new CartItem();
      cartItem.setItem(item);
      cartItem.setQuantity(0);
      cartItem.setInStock(isInStock);
      itemMap.put(item.getItemId(), cartItem);
      itemList.add(cartItem);
    }
    cartItem.incrementQuantity();
  }


  public Item removeItemById(String itemId) {
    CartItem cartItem = (CartItem) itemMap.remove(itemId);
    if (cartItem == null) {
      return null;
    } else {
      itemList.remove(cartItem);
      return cartItem.getItem();
    }
  }

  public void incrementQuantityByItemId(String itemId) {
    CartItem cartItem = (CartItem) itemMap.get(itemId);
    cartItem.incrementQuantity();
  }

  public void setQuantityByItemId(String itemId, int quantity) {
    CartItem cartItem = (CartItem) itemMap.get(itemId);
    cartItem.setQuantity(quantity);
  }

  public BigDecimal getSubTotal() {
    BigDecimal subTotal = new BigDecimal("0");
    Iterator items = getAllCartItems();
    while (items.hasNext()) {
      CartItem cartItem = (CartItem) items.next();
      Item item = cartItem.getItem();
      BigDecimal listPrice = item.getListPrice();
      BigDecimal quantity = new BigDecimal(String.valueOf(cartItem.getQuantity()));
      subTotal = subTotal.add(listPrice.multiply(quantity));
    }
    return subTotal;
  }

}
