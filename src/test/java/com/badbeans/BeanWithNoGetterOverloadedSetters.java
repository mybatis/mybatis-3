package com.badbeans;

public class BeanWithNoGetterOverloadedSetters {

  private String value;


  public void setValue(String value) {
    this.value = value;
  }

  public void setValue(Integer value) {
    this.value = value.toString();
  }

}
