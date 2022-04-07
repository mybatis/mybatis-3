/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.sptests;

public class Parameter {
  private Integer addend1;
  private Integer addend2;
  private Integer sum;

  public Integer getAddend1() {
    return addend1;
  }

  public void setAddend1(Integer addend1) {
    this.addend1 = addend1;
  }

  public Integer getAddend2() {
    return addend2;
  }

  public void setAddend2(Integer addend2) {
    this.addend2 = addend2;
  }

  public Integer getSum() {
    return sum;
  }

  public void setSum(Integer sum) {
    this.sum = sum;
  }
}
