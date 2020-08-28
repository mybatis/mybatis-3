/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.reflection.property;

public class PropertyTestClass extends SuperPropertyTestClass{

  private String testStr;
  private Integer testInt;
  private Boolean testBool;

  public PropertyTestClass(String supStr, String testStr, Integer testInt, Boolean testBool) {
    super(supStr);
    this.testStr = testStr;
    this.testInt = testInt;
    this.testBool = testBool;
  }

  public PropertyTestClass() {
    super();
  }

  public String getTestStr() {
    return testStr;
  }

  public void setTestStr(String testStr) {
    this.testStr = testStr;
  }

  public Integer getTestInt() {
    return testInt;
  }

  public void setTestInt(Integer testInt) {
    this.testInt = testInt;
  }

  public Boolean getTestBool() {
    return testBool;
  }

  public void setTestBool(Boolean testBool) {
    this.testBool = testBool;
  }
}
