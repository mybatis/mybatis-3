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
package com.ibatis.common.util;

import junit.framework.TestCase;

import java.util.Iterator;

public class PaginatedArrayListTest extends TestCase {

  private PaginatedArrayList smallPageList;
  private PaginatedArrayList oddPageList;
  private PaginatedArrayList evenPageList;

  protected void setUp() throws Exception {
    smallPageList = new PaginatedArrayList(5);
    smallPageList.add(new Integer(0));
    smallPageList.add(new Integer(1));
    smallPageList.add(new Integer(2));


    oddPageList = new PaginatedArrayList(5);
    oddPageList.add(new Integer(0));
    oddPageList.add(new Integer(1));
    oddPageList.add(new Integer(2));
    oddPageList.add(new Integer(3));
    oddPageList.add(new Integer(4));
    oddPageList.add(new Integer(5));
    oddPageList.add(new Integer(6));
    oddPageList.add(new Integer(7));
    oddPageList.add(new Integer(8));
    oddPageList.add(new Integer(9));
    oddPageList.add(new Integer(10));
    oddPageList.add(new Integer(11));
    oddPageList.add(new Integer(12));
    oddPageList.add(new Integer(13));
    oddPageList.add(new Integer(14));
    oddPageList.add(new Integer(15));
    oddPageList.add(new Integer(16));
    oddPageList.add(new Integer(17));

    evenPageList = new PaginatedArrayList(5);
    evenPageList.add(new Integer(0));
    evenPageList.add(new Integer(1));
    evenPageList.add(new Integer(2));
    evenPageList.add(new Integer(3));
    evenPageList.add(new Integer(4));
    evenPageList.add(new Integer(5));
    evenPageList.add(new Integer(6));
    evenPageList.add(new Integer(7));
    evenPageList.add(new Integer(8));
    evenPageList.add(new Integer(9));
    evenPageList.add(new Integer(10));
    evenPageList.add(new Integer(11));
    evenPageList.add(new Integer(12));
    evenPageList.add(new Integer(13));
    evenPageList.add(new Integer(14));

  }

  public void testOddPaginatedIterator() {

    assertEquals(true, oddPageList.isFirstPage());
    assertEquals(false, oddPageList.isPreviousPageAvailable());

    int count = 0;
    Iterator i = oddPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(5, count);

    oddPageList.nextPage();

    count = 0;
    i = oddPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(5, count);

    oddPageList.nextPage();

    assertEquals(true, oddPageList.isMiddlePage());

    count = 0;
    i = oddPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(5, count);

    oddPageList.nextPage();

    count = 0;
    i = oddPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(3, count);

    assertEquals(true, oddPageList.isLastPage());
    assertEquals(false, oddPageList.isNextPageAvailable());

    oddPageList.nextPage();

    assertEquals(true, oddPageList.isLastPage());
    assertEquals(false, oddPageList.isNextPageAvailable());

    oddPageList.previousPage();

    assertEquals(new Integer(10), oddPageList.get(0));
    assertEquals(new Integer(12), oddPageList.get(2));

    oddPageList.gotoPage(500);

    assertEquals(new Integer(0), oddPageList.get(0));
    assertEquals(new Integer(4), oddPageList.get(4));

    oddPageList.gotoPage(-500);

    assertEquals(new Integer(15), oddPageList.get(0));
    assertEquals(new Integer(17), oddPageList.get(2));
  }


  public void testEvenPaginatedIterator() {

    assertEquals(true, evenPageList.isFirstPage());
    assertEquals(false, evenPageList.isPreviousPageAvailable());

    int count = 0;
    Iterator i = evenPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(5, count);

    evenPageList.nextPage();

    assertEquals(true, evenPageList.isMiddlePage());

    count = 0;
    i = evenPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(5, count);

    evenPageList.nextPage();

    count = 0;
    i = evenPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(5, count);

    assertEquals(true, evenPageList.isLastPage());
    assertEquals(false, evenPageList.isNextPageAvailable());

    evenPageList.nextPage();

    assertEquals(new Integer(10), evenPageList.get(0));
    assertEquals(new Integer(14), evenPageList.get(4));

    evenPageList.previousPage();

    assertEquals(new Integer(5), evenPageList.get(0));
    assertEquals(new Integer(9), evenPageList.get(4));

    evenPageList.gotoPage(500);

    assertEquals(new Integer(0), evenPageList.get(0));
    assertEquals(new Integer(4), evenPageList.get(4));

    evenPageList.gotoPage(-500);

    assertEquals(new Integer(10), evenPageList.get(0));
    assertEquals(new Integer(14), evenPageList.get(4));
  }

  public void testSmallPaginatedIterator() {

    assertEquals(true, smallPageList.isFirstPage());
    assertEquals(true, smallPageList.isLastPage());
    assertEquals(false, smallPageList.isMiddlePage());
    assertEquals(false, smallPageList.isPreviousPageAvailable());
    assertEquals(false, smallPageList.isNextPageAvailable());

    int count = 0;
    Iterator i = smallPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(3, count);

    smallPageList.nextPage();

    count = 0;
    i = smallPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(3, count);


    assertEquals(true, smallPageList.isFirstPage());
    assertEquals(true, smallPageList.isLastPage());
    assertEquals(false, smallPageList.isMiddlePage());
    assertEquals(false, smallPageList.isPreviousPageAvailable());
    assertEquals(false, smallPageList.isNextPageAvailable());

    smallPageList.nextPage();

    count = 0;
    i = smallPageList.iterator();
    while (i.hasNext()) {
      i.next();
      count++;
    }
    assertEquals(3, count);

    smallPageList.nextPage();

    assertEquals(new Integer(0), smallPageList.get(0));
    assertEquals(new Integer(2), smallPageList.get(2));

    smallPageList.previousPage();

    assertEquals(new Integer(0), smallPageList.get(0));
    assertEquals(new Integer(2), smallPageList.get(2));

    smallPageList.gotoPage(500);

    assertEquals(new Integer(0), smallPageList.get(0));
    assertEquals(new Integer(2), smallPageList.get(2));

    smallPageList.gotoPage(-500);

    assertEquals(new Integer(0), smallPageList.get(0));
    assertEquals(new Integer(2), smallPageList.get(2));


    assertEquals(true, smallPageList.isFirstPage());
    assertEquals(true, smallPageList.isLastPage());
    assertEquals(false, smallPageList.isMiddlePage());
    assertEquals(false, smallPageList.isPreviousPageAvailable());
    assertEquals(false, smallPageList.isNextPageAvailable());
  }
}
