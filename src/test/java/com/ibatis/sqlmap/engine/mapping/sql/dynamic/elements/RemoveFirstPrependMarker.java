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
package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

/*
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

  /*
   * @return Returns the removeFirstPrepend.
   */
  public boolean isRemoveFirstPrepend() {
    return removeFirstPrepend;
  }

  /*
   * @param removeFirstPrepend The removeFirstPrepend to set.
   */
  public void setRemoveFirstPrepend(boolean removeFirstPrepend) {
    this.removeFirstPrepend = removeFirstPrepend;
  }

  /*
   * @return Returns the sqlTag.
   */
  public SqlTag getSqlTag() {
    return tag;
  }

}
