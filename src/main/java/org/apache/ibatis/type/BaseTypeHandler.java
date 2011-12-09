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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseTypeHandler<T> implements TypeHandler<T> {

  public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException {
    if (parameter == null) {
      if (jdbcType == null) {
        try {
          ps.setNull(i, JdbcType.OTHER.TYPE_CODE);
        } catch (SQLException e) {
          throw new TypeException("Error setting null parameter.  Most JDBC drivers require that the JdbcType must be specified for all nullable parameters. Cause: " + e, e);
        }
      } else {
        ps.setNull(i, jdbcType.TYPE_CODE);
      }
    } else {
      setNonNullParameter(ps, i, parameter, jdbcType);
    }
  }

  public T getResult(ResultSet rs, String columnName)
      throws SQLException {
    T result = getNullableResult(rs, columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return result;
    }
  }

  public T getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    T result = getNullableResult(rs, columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return result;
    }
  }
  
  public T getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    T result = getNullableResult(cs, columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return result;
    }
  }

  public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType)
      throws SQLException;

  public abstract T getNullableResult(ResultSet rs, String columnName)
      throws SQLException;

  public abstract T getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException;
  
  public abstract T getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException;

}

