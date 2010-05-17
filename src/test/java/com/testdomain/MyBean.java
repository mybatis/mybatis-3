package com.testdomain;

import java.util.List;

/**
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
