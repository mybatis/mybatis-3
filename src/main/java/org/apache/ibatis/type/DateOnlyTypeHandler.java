package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DateOnlyTypeHandler extends BaseTypeHandler {

  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setDate(i, new java.sql.Date(((Date) parameter).getTime()));
  }

  public Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    java.sql.Date sqlDate = rs.getDate(columnName);
    if (sqlDate != null) {
      return new java.util.Date(sqlDate.getTime());
    }
    return null;
  }

  public Object getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    java.sql.Date sqlDate = cs.getDate(columnIndex);
    if (sqlDate != null) {
      return new java.util.Date(sqlDate.getTime());
    }
    return null;
  }

}
