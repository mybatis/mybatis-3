package com.testdomain;

import java.math.BigDecimal;

/**
 * Used in testing the ResultObjectFactory
 *
 * @author Jeff Butler
 */
public interface IItem {
  String getAttribute1();

  void setAttribute1(String attribute1);

  String getAttribute2();

  void setAttribute2(String attribute2);

  String getAttribute3();

  void setAttribute3(String attribute3);

  String getAttribute4();

  void setAttribute4(String attribute4);

  String getAttribute5();

  void setAttribute5(String attribute5);

  String getItemId();

  void setItemId(String itemId);

  BigDecimal getListPrice();

  void setListPrice(BigDecimal listPrice);

  String getProductId();

  void setProductId(String productId);

  String getStatus();

  void setStatus(String status);

  ISupplier getSupplier();

  void setSupplier(ISupplier supplier);

  BigDecimal getUnitCost();

  void setUnitCost(BigDecimal unitCost);
}
