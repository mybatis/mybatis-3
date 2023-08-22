/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.submitted.extendresultmap;

import java.util.Objects;

public class TestModel {

  private String a;
  private String b;

  public TestModel() {
  }

  public TestModel(String a, String b) {
    this.a = a;
    this.b = b;
  }

  public String getA() {
    return a;
  }

  public void setA(String a) {
    this.a = a;
  }

  public String getB() {
    return b;
  }

  public void setB(String b) {
    this.b = b;
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
      return false;
    }
    TestModel other = (TestModel) obj;
    if (!Objects.equals(a, other.a)) {
      return false;
    }
    if (!Objects.equals(b, other.b)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "TestModel [a=" + a + ", b=" + b + "]";
  }
}
