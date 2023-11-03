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

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * @author Lambert Rao
 */
public class IncludeSqlSource implements SqlSource {
  private final boolean isDynamic;

  private SqlNode rootSqlNode;

  private String sqlString;

  private final SqlSource sqlSource;

  public IncludeSqlSource(boolean isDynamic, SqlNode rootSqlNode, SqlSource sqlSource) {
    this.isDynamic = isDynamic;
    this.rootSqlNode = rootSqlNode;
    this.sqlSource = sqlSource;
  }

  public IncludeSqlSource(boolean isDynamic, String sqlString, SqlSource sqlSource) {
    this.isDynamic = isDynamic;
    this.sqlString = sqlString;
    this.sqlSource = sqlSource;
  }

  public boolean isDynamic() {
    return isDynamic;
  }

  public SqlNode getRootSqlNode() {
    return rootSqlNode;
  }

  public String getSqlString() {
    return sqlString;
  }

  public SqlSource getSqlSource() {
    return sqlSource;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return sqlSource.getBoundSql(parameterObject);
  }
}
