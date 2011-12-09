/*
 *    Copyright 2009-2011 The MyBatis Team
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
package org.apache.ibatis.executor;

import org.apache.ibatis.mapping.MappedStatement;

public class BatchResult {

  private final MappedStatement mappedStatement;
  private final String sql;
  private final Object parameterObject;

  private int[] updateCounts;

  public BatchResult(MappedStatement mappedStatement, String sql, Object parameterObject) {
    super();
    this.mappedStatement = mappedStatement;
    this.sql = sql;
    this.parameterObject = parameterObject;
  }

  public MappedStatement getMappedStatement() {
    return mappedStatement;
  }

  public String getSql() {
    return sql;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public int[] getUpdateCounts() {
    return updateCounts;
  }

  public void setUpdateCounts(int[] updateCounts) {
    this.updateCounts = updateCounts;
  }

}
