package org.apache.ibatis.type;

import java.io.StringReader;
import java.sql.*;

public class NClobTypeHandler extends BaseTypeHandler<String> {


  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    StringReader reader = new StringReader(parameter);
//    ps.setNCharacterStream(i, reader, s.length());
    ps.setCharacterStream(i, reader, parameter.length());
  }

  public String getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    String value = "";
//    Clob clob = rs.getNClob(columnName);
    Clob clob = rs.getClob(columnName);
    if (clob != null) {
      int size = (int) clob.length();
      value = clob.getSubString(1, size);
    }
    return value;
  }

  public String getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    String value = "";
//    Clob clob = cs.getNClob(columnIndex);
    Clob clob = cs.getClob(columnIndex);
    if (clob != null) {
      int size = (int) clob.length();
      value = clob.getSubString(1, size);
    }
    return value;
  }

}