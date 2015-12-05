package org.apache.ibatis.submitted.emptycollection;

public class TodoItem {

  @Override
  public String toString() {
    return "TodoItem [order=" + order + ", description=" + description + "]";
  }

  private int order;
  private String description;

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
