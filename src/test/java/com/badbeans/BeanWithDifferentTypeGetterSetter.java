package com.badbeans;

public class BeanWithDifferentTypeGetterSetter {

  private String value;


  public String getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value.toString();
  }
}
