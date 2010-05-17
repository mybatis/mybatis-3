package com.testdomain;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {

  private String categoryId;
  private Category parentCategory;
  private String name;
  private String description;
  private List itemList;
  private List productList;

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

  public List getProductList() {
    return productList;
  }

  public void setProductList(List productList) {
    this.productList = productList;
  }

  public Category getParentCategory() {
    return parentCategory;
  }

  public List getItemList() {
    return itemList;
  }

  public void setItemList(List itemList) {
    this.itemList = itemList;
  }

  public void setParentCategory(Category parentCategory) {
    this.parentCategory = parentCategory;
  }
}
