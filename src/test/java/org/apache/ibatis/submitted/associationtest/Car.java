package org.apache.ibatis.submitted.associationtest;

public class Car {
  private int id;
  private String type;
  private Engine engine;
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Engine getEngine() {
    return engine;
  }
  public void setEngine(Engine engine) {
    this.engine = engine;
  }

}
