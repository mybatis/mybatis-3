/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.handle_by_jdbc_type;

public class BoolsBean {

  private Integer id;
  private boolean b1;
  private boolean b2;
  private char c;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public boolean isB1() {
    return b1;
  }

  public void setB1(boolean b1) {
    this.b1 = b1;
  }

  public boolean isB2() {
    return b2;
  }

  public void setB2(boolean b2) {
    this.b2 = b2;
  }

  public char getC() {
    return c;
  }

  public void setC(char c) {
    this.c = c;
  }

}
