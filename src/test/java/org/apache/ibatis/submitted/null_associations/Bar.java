package org.apache.ibatis.submitted.null_associations;

import java.io.Serializable;

public class Bar implements Serializable {

  private long field1;
  private long field2;
  private long field3;

  public Bar() {
    super();
  }

  public Bar(long field1, long field2, long field3) {
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

  public long getField2() {
    return field2;
  }

  public void setField2(long field2) {
    this.field2 = field2;
  }

  public long getField3() {
    return field3;
  }

  public void setField3(long field3) {
    this.field3 = field3;
  }

}
