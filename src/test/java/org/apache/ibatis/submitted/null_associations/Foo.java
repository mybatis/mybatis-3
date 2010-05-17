package org.apache.ibatis.submitted.null_associations;

import java.io.Serializable;

public class Foo implements Serializable {

  private long field1;
  private Bar field2;
  private boolean field3;

  public Foo() {
    super();
  }

  public Foo(long field1, Bar field2, boolean field3) {
    super();
    this.field1 = field1;
    this.field2 = field2;
    this.field3 = field3;
  }

  public long getField1() {
    return field1;
  }

  public void setField1(long field1) {
    this.field1 = field1;
  }

  public Bar getField2() {
    return field2;
  }

  public void setField2(Bar field2) {
    this.field2 = field2;
  }

  public boolean isField3() {
    return field3;
  }

  public void setField3(boolean field3) {
    this.field3 = field3;
  }

}
