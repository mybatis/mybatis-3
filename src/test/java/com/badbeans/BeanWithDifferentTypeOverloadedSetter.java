package com.badbeans;

public class BeanWithDifferentTypeOverloadedSetter {

  private String value;


  public String getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value.toString();
  }

  public void setValue(Integer value) {
    this.value = value.toString();
  }
}
