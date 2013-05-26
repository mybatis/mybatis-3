/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
