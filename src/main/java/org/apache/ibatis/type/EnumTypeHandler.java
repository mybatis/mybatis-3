package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnumTypeHandler extends BaseTypeHandler implements TypeHandler {

  private Class type;

  public EnumTypeHandler(Class type) {
    this.type = type;
  }

  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.toString());
  }

  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String s = rs.getString(columnName);
    return s == null ? null : Enum.valueOf(type, s);
  }

  public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String s = cs.getString(columnIndex);
    return s == null ? null : Enum.valueOf(type, s);
  }

}