package org.apache.ibatis.submitted.overwritingproperties;

import java.io.Serializable;

public class Foo implements Serializable {

  private Long field1;
  private Bar field2;
  private Integer field3;
  private Integer field4;

  public Foo() {
    super();
  }

  public Foo(Long field1, Bar field2, Integer field3, Integer field4) {
    this.field1 = field1;
    this.field2 = field2;
    this.field3 = field3;
    this.field4 = field4;
  }

  public Long getField1() {
    return field1;
  }

  public void setField1(Long field1) {
    this.field1 = field1;
  }

  public Bar getField2() {
    return field2;
  }

  public void setField2(Bar field2) {
    this.field2 = field2;
  }

  public Integer getField3() {
    return field3;
  }

  public void setField3(Integer field3) {
    this.field3 = field3;
  }

  public Integer getField4() {
    return field4;
  }

  public void setField4(Integer field4) {
    this.field4 = field4;
  }
}
