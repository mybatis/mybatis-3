/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.executor.dialect;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.PageRowBounds;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * support derby 10.6+
 *
 * @author liuzenghui
 */
public class DerbyDialect implements Dialect {

  @Override
  public boolean doCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
    if(rowBounds instanceof PageRowBounds){
      return true;
    }
    return false;
  }

  @Override
  public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
    String sql = boundSql.getSql();
    StringBuilder countBuilder = new StringBuilder(sql.length() + 40);
    countBuilder.append("SELECT COUNT(0) FROM (");
    countBuilder.append(sql);
    countBuilder.append(") TEMP");
    return countBuilder.toString();
  }

  @Override
  public void afterCount(long count, Object parameterObject, RowBounds rowBounds) {
    PageRowBounds pageRowBounds = (PageRowBounds)rowBounds;
    pageRowBounds.setTotal(count);
  }

  @Override
  public boolean doPage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
    return rowBounds != RowBounds.DEFAULT;
  }

  @Override
  public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
    return parameterObject;
  }

  @Override
  public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
    String sql = boundSql.getSql();
    StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
    sqlBuilder.append(sql);
    if(rowBounds.getOffset() != 0){
      sqlBuilder.append(" OFFSET ");
      sqlBuilder.append(rowBounds.getOffset());
      sqlBuilder.append(" ROWS ");
    }
    if(rowBounds.getLimit() != 0){
      sqlBuilder.append(" FETCH NEXT ");
      sqlBuilder.append(rowBounds.getLimit());
      sqlBuilder.append(" ROWS ONLY");
    }
    return sqlBuilder.toString();
  }

  @Override
  public <E> List<E> afterPage(List<E> pageList, Object parameterObject, RowBounds rowBounds) {
    return pageList;
  }
}
