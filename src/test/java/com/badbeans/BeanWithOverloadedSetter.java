package com.badbeans;

public class BeanWithOverloadedSetter {

  private String value;


  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setValue(Integer value) {
    this.value = value.toString();
  }
}
