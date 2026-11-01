/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.typebasedtypehandlerresolution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;

public class TypeAwareTypeHandler implements TypeHandler<Object> {
  private final Class<?> rawType;
  private final Class<?> typeArg;

  public TypeAwareTypeHandler(Type type) {
    super();
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = ((ParameterizedType) type);
      rawType = (Class<?>) parameterizedType.getRawType();
      typeArg = (Class<?>) parameterizedType.getActualTypeArguments()[0];
    } else {
      rawType = (Class<?>) type;
      typeArg = null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    if (FuzzyBean.class.equals(rawType)) {
      if (String.class.equals(typeArg)) {
        ps.setString(i, ((FuzzyBean<String>) parameter).getValue());
      } else if (Integer.class.equals(typeArg)) {
        ps.setInt(i, ((FuzzyBean<Integer>) parameter).getValue());
      } else {
        throw new TypeException("Unknown typeArg for FuzzyBean : " + typeArg);
      }
    } else if (LocalDate.class.equals(rawType)) {
      LocalDate d = (LocalDate) parameter;
      ps.setInt(i, d == null ? 0 : d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth());
    } else {
      throw new TypeException("Unknown rawType : " + rawType);
    }
  }

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    if (FuzzyBean.class.equals(rawType)) {
      if (String.class.equals(typeArg)) {
        return new FuzzyBean<String>(rs.getString(columnName));
      } else if (Integer.class.equals(typeArg)) {
        return new FuzzyBean<Integer>(rs.getInt(columnName));
      }
    } else if (LocalDate.class.equals(rawType)) {
      int v = rs.getInt(columnName);
      return v == 0 ? null : LocalDate.of(v / 10000, v / 100 % 100, v % 100);
    }
    return null;
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    if (FuzzyBean.class.equals(rawType)) {
      if (String.class.equals(typeArg)) {
        return new FuzzyBean<String>(rs.getString(columnIndex));
      } else if (Integer.class.equals(typeArg)) {
        return new FuzzyBean<Integer>(rs.getInt(columnIndex));
      }
    } else if (LocalDate.class.equals(rawType)) {
      int v = rs.getInt(columnIndex);
      return v == 0 ? null : LocalDate.of(v / 10000, v / 100 % 100, v % 100);
    }
    return null;
  }

  @Override
  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    return null;
  }
}
