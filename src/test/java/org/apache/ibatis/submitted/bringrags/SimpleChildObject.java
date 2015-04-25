package org.apache.ibatis.submitted.bringrags;

public class SimpleChildObject {

  private String id;
  private SimpleObject parentSimpleObject;

  public SimpleChildObject(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public SimpleObject getSimpleObject() {
    return parentSimpleObject;
  }

}