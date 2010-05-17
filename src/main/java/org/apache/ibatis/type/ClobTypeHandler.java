package org.apache.ibatis.type;

import java.io.StringReader;
import java.sql.*;

public class ClobTypeHandler extends BaseTypeHandler {


  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    String s = (String) parameter;
    StringReader reader = new StringReader(s);
    ps.setCharacterStream(i, reader, s.length());
  }

  public Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    String value = "";
    Clob clob = rs.getClob(columnName);
    if (clob != null) {
      int size = (int) clob.length();
      value = clob.getSubString(1, size);
    }
    return value;
  }

  public Object getNullableResult(CallableStatement cs, int columnIndex)
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
