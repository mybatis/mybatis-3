package com.testdomain;

/**
 * Created by IntelliJ IDEA.
 * User: cbegin
 * Date: May 14, 2005
 * Time: 1:39:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class PrivateBook extends Document {

  private Integer pages;

  private Integer getPages() {
    return pages;
  }

  private void setPages(Integer pages) {
    this.pages = pages;
  }

}
