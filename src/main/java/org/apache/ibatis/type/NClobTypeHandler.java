package org.apache.ibatis.type;

import java.io.StringReader;
import java.sql.*;

public class NClobTypeHandler extends BaseTypeHandler<String> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    StringReader reader = new StringReader(parameter);
    ps.setCharacterStream(i, reader, parameter.length());
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    String value = "";
    Clob clob = rs.getClob(columnName);
    if (clob != null) {
      int size = (int) clob.length();
      value = clob.getSubString(1, size);
    }
    return value;
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    String value = "";
    Clob clob = rs.getClob(columnIndex);
    if (clob != null) {
      int size = (int) clob.length();
      value = clob.getSubString(1, size);
    }
    return value;
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    String value = "";
    Clob clob = cs.getClob(columnIndex);
    if (clob != null) {
      int size = (int) clob.length();
      value = clob.getSubString(1, size);
    }
    return value;
  }
}