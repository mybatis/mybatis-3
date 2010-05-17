package com.ibatis.sqlmap.client.event;


/**
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

  /**
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
