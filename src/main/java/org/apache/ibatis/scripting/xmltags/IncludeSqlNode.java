/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import java.util.Objects;

/**
 * @author hssy
 */
public class IncludeSqlNode implements SqlNode {

  private SqlNode sqlNode;

  private String sqlString;

  @Override
  public boolean apply(DynamicContext context) {
    if (Objects.nonNull(sqlNode)) {
      sqlNode.apply(context);
    } else {
      context.appendSql(sqlString);
    }

    return true;
  }

  public void setSqlNode(SqlNode sqlNode) {
    this.sqlNode = sqlNode;
  }

  public void setSqlString(String sqlString) {
    this.sqlString = sqlString;
  }

  public SqlNode getSqlNode() {
    return sqlNode;
  }

  public String getSqlString() {
    return sqlString;
  }
}
