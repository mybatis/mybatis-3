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
package org.apache.ibatis.type;

import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * @author Clinton Begin
 */
public class ClobTypeHandler extends BaseTypeHandler<String> {
  public static final ClobTypeHandler INSTANCE = new ClobTypeHandler();

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    StringReader reader = new StringReader(parameter);
    ps.setCharacterStream(i, reader, parameter.length());
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Clob clob = rs.getClob(columnName);
    return toString(clob);
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    Clob clob = rs.getClob(columnIndex);
    return toString(clob);
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    Clob clob = cs.getClob(columnIndex);
    return toString(clob);
  }

  private String toString(Clob clob) throws SQLException {
    if (clob == null) {
      return null;
    }
    String value;
    try {
      value = clob.getSubString(1, (int) clob.length());
    } catch (SQLException | RuntimeException e) {
      freeAfterFailure(clob, e);
      throw e;
    }
    free(clob);
    return value;
  }

  private void free(Clob clob) throws SQLException {
    try {
      clob.free();
    } catch (SQLFeatureNotSupportedException ignored) {
      // Some JDBC drivers do not support freeing LOBs.
    }
  }

  private void freeAfterFailure(Clob clob, Throwable failure) {
    try {
      free(clob);
    } catch (SQLException e) {
      failure.addSuppressed(e);
    }
  }

}
