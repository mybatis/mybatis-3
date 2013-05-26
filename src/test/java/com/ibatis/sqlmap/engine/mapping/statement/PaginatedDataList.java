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
package com.ibatis.sqlmap.engine.mapping.statement;


import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapExecutor;

import java.sql.SQLException;
import java.util.*;

public class PaginatedDataList implements PaginatedList {

  private SqlMapExecutor sqlMapExecutor;
  private String statementName;
  private Object parameterObject;

  private int pageSize;
  private int index;

  private List prevPageList;
  private List currentPageList;
  private List nextPageList;

  public PaginatedDataList(SqlMapExecutor sqlMapExecutor, String statementName, Object parameterObject, int pageSize)
      throws SQLException {
    this.sqlMapExecutor = sqlMapExecutor;
    this.statementName = statementName;
    this.parameterObject = parameterObject;
    this.pageSize = pageSize;
    this.index = 0;
    pageTo(0);
  }

  private void pageForward() {
    try {
      prevPageList = currentPageList;
      currentPageList = nextPageList;
      nextPageList = getList(index + 1, pageSize);
    } catch (SQLException e) {
      throw new RuntimeException("Unexpected error while repaginating paged list.  Cause: " + e, e);
    }
  }

  private void pageBack() {
    try {
      nextPageList = currentPageList;
      currentPageList = prevPageList;
      if (index > 0) {
        prevPageList = getList(index - 1, pageSize);
      } else {
        prevPageList = new ArrayList();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Unexpected error while repaginating paged list.  Cause: " + e, e);
    }
  }

  private void safePageTo(int idx) {
    try {
      pageTo(idx);
    } catch (SQLException e) {
      throw new RuntimeException("Unexpected error while repaginating paged list.  Cause: " + e, e);
    }
  }

  public void pageTo(int idx) throws SQLException {
    index = idx;

    List list;

    if (idx < 1) {
      list = getList(idx, pageSize * 2);
    } else {
      list = getList(idx - 1, pageSize * 3);
    }

    if (list.size() < 1) {
      prevPageList = new ArrayList(0);
      currentPageList = new ArrayList(0);
      nextPageList = new ArrayList(0);
    } else {
      if (idx < 1) {
        prevPageList = new ArrayList(0);
        if (list.size() <= pageSize) {
          currentPageList = list.subList(0, list.size());
          nextPageList = new ArrayList(0);
        } else {
          currentPageList = list.subList(0, pageSize);
          nextPageList = list.subList(pageSize, list.size());
        }
      } else {
        if (list.size() <= pageSize) {
          prevPageList = list.subList(0, list.size());
          currentPageList = new ArrayList(0);
          nextPageList = new ArrayList(0);
        } else if (list.size() <= pageSize * 2) {
          prevPageList = list.subList(0, pageSize);
          currentPageList = list.subList(pageSize, list.size());
          nextPageList = new ArrayList(0);
        } else {
          prevPageList = list.subList(0, pageSize);
          currentPageList = list.subList(pageSize, pageSize * 2);
          nextPageList = list.subList(pageSize * 2, list.size());
        }
      }
    }

  }


  private List getList(int idx, int localPageSize) throws SQLException {
    return sqlMapExecutor.queryForList(statementName, parameterObject, (idx) * pageSize, localPageSize);
  }


  public boolean nextPage() {
    if (isNextPageAvailable()) {
      index++;
      pageForward();
      return true;
    } else {
      return false;
    }
  }

  public boolean previousPage() {
    if (isPreviousPageAvailable()) {
      index--;
      pageBack();
      return true;
    } else {
      return false;
    }
  }

  public void gotoPage(int pageNumber) {
    safePageTo(pageNumber);
  }

  public int getPageSize() {
    return pageSize;
  }

  public boolean isFirstPage() {
    return index == 0;
  }

  public boolean isMiddlePage() {
    return !(isFirstPage() || isLastPage());
  }

  public boolean isLastPage() {
    return nextPageList.size() < 1;
  }

  public boolean isNextPageAvailable() {
    return nextPageList.size() > 0;
  }

  public boolean isPreviousPageAvailable() {
    return prevPageList.size() > 0;
  }

  public int size() {
    return currentPageList.size();
  }

  public boolean isEmpty() {
    return currentPageList.isEmpty();
  }

  public boolean contains(Object o) {
    return currentPageList.contains(o);
  }

  public Iterator iterator() {
    return currentPageList.iterator();
  }

  public Object[] toArray() {
    return currentPageList.toArray();
  }

  public Object[] toArray(Object a[]) {
    return currentPageList.toArray(a);
  }

  public boolean containsAll(Collection c) {
    return currentPageList.containsAll(c);
  }

  public Object get(int index) {
    return currentPageList.get(index);
  }

  public int indexOf(Object o) {
    return currentPageList.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return currentPageList.lastIndexOf(o);
  }

  public ListIterator listIterator() {
    return currentPageList.listIterator();
  }

  public ListIterator listIterator(int index) {
    return currentPageList.listIterator(index);
  }

  public List subList(int fromIndex, int toIndex) {
    return currentPageList.subList(fromIndex, toIndex);
  }

  public boolean add(Object o) {
    return currentPageList.add(o);
  }

  public boolean remove(Object o) {
    return currentPageList.remove(o);
  }

  public boolean addAll(Collection c) {
    return currentPageList.addAll(c);
  }

  public boolean addAll(int index, Collection c) {
    return currentPageList.addAll(index, c);
  }

  public boolean removeAll(Collection c) {
    return currentPageList.removeAll(c);
  }

  public boolean retainAll(Collection c) {
    return currentPageList.retainAll(c);
  }

  public void clear() {
    currentPageList.clear();
  }

  public Object set(int index, Object element) {
    return currentPageList.set(index, element);
  }

  public void add(int index, Object element) {
    currentPageList.add(index, element);
  }

  public Object remove(int index) {
    return currentPageList.remove(index);
  }


  public int getPageIndex() {
    return index;
  }

}
