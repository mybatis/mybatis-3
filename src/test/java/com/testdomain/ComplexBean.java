package com.testdomain;

import java.io.Serializable;
import java.util.Map;

public class ComplexBean implements Serializable {

  private Map map;

  public Map getMap() {
    return map;
  }

  public void setMap(Map map) {
    this.map = map;
  }

}
