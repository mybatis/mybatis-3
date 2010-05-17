package com.testdomain;

import java.math.BigDecimal;

/**
 * Used in testing the ResultObjectFactory
 *
 * @author Jeff Butler
 */
public class IItemImpl implements IItem {
  private String itemId;
  private String productId;
  private BigDecimal listPrice;
  private BigDecimal unitCost;
  private String status;
  private String attribute1;
  private String attribute2;
  private String attribute3;
  private String attribute4;
  private String attribute5;
  private ISupplier supplier;

  /**
   *
   */
  public IItemImpl() {
    super();
  }

  public String getAttribute1() {
    return attribute1;
  }

  public void setAttribute1(String attribute1) {
    this.attribute1 = attribute1;
  }

  public String getAttribute2() {
    return attribute2;
  }

  public void setAttribute2(String attribute2) {
    this.attribute2 = attribute2;
  }

  public String getAttribute3() {
    return attribute3;
  }

  public void setAttribute3(String attribute3) {
    this.attribute3 = attribute3;
  }

  public String getAttribute4() {
    return attribute4;
  }

  public void setAttribute4(String attribute4) {
    this.attribute4 = attribute4;
  }

  public String getAttribute5() {
    return attribute5;
  }

  public void setAttribute5(String attribute5) {
    this.attribute5 = attribute5;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public BigDecimal getListPrice() {
    return listPrice;
  }

  public void setListPrice(BigDecimal listPrice) {
    this.listPrice = listPrice;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public ISupplier getSupplier() {
    return supplier;
  }

  public void setSupplier(ISupplier supplier) {
    this.supplier = supplier;
  }

  public BigDecimal getUnitCost() {
    return unitCost;
  }

  public void setUnitCost(BigDecimal unitCost) {
    this.unitCost = unitCost;
  }

}
