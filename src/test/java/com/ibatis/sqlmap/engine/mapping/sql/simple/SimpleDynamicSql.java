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
package com.ibatis.sqlmap.engine.mapping.sql.simple;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;
import java.util.StringTokenizer;
import org.apache.ibatis.reflection.SystemMetaObject;

public class SimpleDynamicSql implements Sql {

  private static final String ELEMENT_TOKEN = "$";

  private String sqlStatement;
  private List<ParameterMapping> parameterMappings;
  TypeHandlerRegistry typeHandlerRegistry;

  public SimpleDynamicSql(String sqlStatement, List<ParameterMapping> parameterMappings, TypeHandlerRegistry typeHandlerRegistry) {
    this.sqlStatement = sqlStatement;
    this.parameterMappings = parameterMappings;
    this.typeHandlerRegistry = typeHandlerRegistry;
  }

  public String getSql(Object parameterObject) {
    return processDynamicElements(sqlStatement, parameterObject);
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    return parameterMappings;
  }

  public static boolean isSimpleDynamicSql(String sql) {
    return sql != null && sql.indexOf(ELEMENT_TOKEN) > -1;
  }

  private String processDynamicElements(String sql, Object parameterObject) {
    StringTokenizer parser = new StringTokenizer(sql, ELEMENT_TOKEN, true);
    StringBuffer newSql = new StringBuffer();

    String token = null;
    String lastToken = null;
    while (parser.hasMoreTokens()) {
      token = parser.nextToken();

      if (ELEMENT_TOKEN.equals(lastToken)) {
        if (ELEMENT_TOKEN.equals(token)) {
          newSql.append(ELEMENT_TOKEN);
          token = null;
        } else {

          Object value = null;
          if (parameterObject != null) {
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
              value = parameterObject;
            } else {
              value = SystemMetaObject.forObject(parameterObject).getValue(token);
            }
          }
          if (value != null) {
            newSql.append(String.valueOf(value));
          }

          token = parser.nextToken();
          if (!ELEMENT_TOKEN.equals(token)) {
            throw new SqlMapException("Unterminated dynamic element in sql (" + sql + ").");
          }
          token = null;
        }
      } else {
        if (!ELEMENT_TOKEN.equals(token)) {
          newSql.append(token);
        }
      }

      lastToken = token;
    }

    return newSql.toString();
  }


}

