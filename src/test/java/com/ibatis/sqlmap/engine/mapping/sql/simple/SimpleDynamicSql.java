package com.ibatis.sqlmap.engine.mapping.sql.simple;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.List;
import java.util.StringTokenizer;

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
              value = MetaObject.forObject(parameterObject).getValue(token);
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

