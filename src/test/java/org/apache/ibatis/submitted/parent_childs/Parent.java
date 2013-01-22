package org.apache.ibatis.submitted.parent_childs;

import java.util.List;

public class Parent {

  private int id;
  private String name;
  private String surName;
  private List<Child> childs;

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getSurName() {
    return surName;
  }
  public void setSurName(String surName) {
    this.surName = surName;
  }
  public List<Child> getChilds() {
    return childs;
  }
  public void setChilds(List<Child> childs) {
    this.childs = childs;
  }

}
