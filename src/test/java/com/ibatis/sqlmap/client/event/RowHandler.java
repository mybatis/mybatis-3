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
package com.ibatis.sqlmap.client.event;


/*
 * Event handler for row by row processing.
 * <p/>
 * The RowHandler interface is used by the SqlMapSession.queryWithRowHandler() method.
 * Generally a RowHandler implementation will perform some row-by-row processing logic
 * in cases where there are too many rows to efficiently load into memory.
 * <p/>
 * Example:
 * <pre>
 * sqlMap.queryWithRowHandler ("findAllEmployees", null, new MyRowHandler()));
 * </pre>
 */
public interface RowHandler {

  /*
   * Handles a single row of a result set.
   * <p/>
   * This method will be called for each row in a result set.  For each row the result map
   * will be applied to build the value object, which is then passed in as the valueObject
   * parameter.
   *
   * @param valueObject The object representing a single row from the query.
   * @see com.ibatis.sqlmap.client.SqlMapSession
   */
  void handleRow(Object valueObject);

}
