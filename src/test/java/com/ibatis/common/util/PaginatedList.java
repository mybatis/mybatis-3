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

import java.util.List;

/*
 * Interface for lists that support paging
 */
public interface PaginatedList extends List {

  /*
   * Returns the maximum number of items per page
   *
   * @return The maximum number of items per page.
   */
  public int getPageSize();

  /*
   * Is the current page the first page?
   *
   * @return True if the current page is the first page or if only
   *         a single page exists.
   */
  public boolean isFirstPage();

  /*
   * Is the current page a middle page (ie not first or last)?
   *
   * @return True if the current page is not the first or last page,
   *         and more than one page exists (always returns false if only a
   *         single page exists).
   */
  public boolean isMiddlePage();

  /*
   * Is the current page the last page?
   *
   * @return True if the current page is the last page or if only
   *         a single page exists.
   */
  public boolean isLastPage();

  /*
   * Is a page available after the current page?
   *
   * @return True if the next page is available
   */
  public boolean isNextPageAvailable();

  /*
   * Is a page available before the current page?
   *
   * @return True if the previous page is available
   */
  public boolean isPreviousPageAvailable();

  /*
   * Moves to the next page after the current page.  If the current
   * page is the last page, wrap to the first page.
   *
   * @return True if the page changed
   */
  public boolean nextPage();

  /*
   * Moves to the page before the current page.  If the current
   * page is the first page, wrap to the last page.
   *
   * @return True if the page changed
   */
  public boolean previousPage();

  /*
   * Moves to a specified page.  If the specified
   * page is beyond the last page, wrap to the first page.
   * If the specified page is before the first page, wrap
   * to the last page.
   *
   * @param pageNumber The page to go to
   */
  public void gotoPage(int pageNumber);

  /*
   * Returns the current page index, which is a zero based integer.
   * All paginated list implementations should know what index they are
   * on, even if they don't know the ultimate boundaries (min/max).
   *
   * @return The current page
   */
  public int getPageIndex();

}
