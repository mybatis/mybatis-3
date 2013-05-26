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
package com.testdomain;

import java.util.List;

/*
 * Created by IntelliJ IDEA.
 * User: bgoodin
 * Date: May 25, 2005
 * Time: 6:57:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyBean {

  public List getMyList() {
    return myList;
  }

  public void setMyList(List myList) {
    this.myList = myList;
  }

  public Object[] getMyArray() {
    return myArray;
  }

  public void setMyArray(Object[] myArray) {
    this.myArray = myArray;
  }

  public int[] getIntArray() {
    return intArray;
  }

  public void setIntArray(int[] intArray) {
    this.intArray = intArray;
  }

  private List myList;
  private Object[] myArray;
  private int[] intArray;

}
