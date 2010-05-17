package com.testdomain;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {

  private String productId;
  private String categoryId;
  private String name;
  private String description;
  private List itemList;

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List getItemList() {
    return itemList;
  }

  public void setItemList(List itemList) {
    this.itemList = itemList;
  }
}
