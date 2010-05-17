package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

/**
 * This inner class i used strictly to house whether the
 * removeFirstPrepend has been used in a particular nested
 * situation.
 *
 * @author Brandon Goodin
 */
class RemoveFirstPrependMarker {

  private boolean removeFirstPrepend;
  private SqlTag tag;

  public RemoveFirstPrependMarker(SqlTag tag, boolean removeFirstPrepend) {
    this.removeFirstPrepend = removeFirstPrepend;
    this.tag = tag;
  }

  /**
   * @return Returns the removeFirstPrepend.
   */
  public boolean isRemoveFirstPrepend() {
    return removeFirstPrepend;
  }

  /**
   * @param removeFirstPrepend The removeFirstPrepend to set.
   */
  public void setRemoveFirstPrepend(boolean removeFirstPrepend) {
    this.removeFirstPrepend = removeFirstPrepend;
  }

  /**
   * @return Returns the sqlTag.
   */
  public SqlTag getSqlTag() {
    return tag;
  }

}
