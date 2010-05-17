package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class TimeOnlyTypeHandler extends BaseTypeHandler {

  public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTime(i, new java.sql.Time(((Date) parameter).getTime()));
  }

  public Object getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    java.sql.Time sqlTime = rs.getTime(columnName);
    if (sqlTime != null) {
      return new java.util.Date(sqlTime.getTime());
    }
    return null;
  }

  public Object getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    java.sql.Time sqlTime = cs.getTime(columnIndex);
    if (sqlTime != null) {
      return new java.util.Date(sqlTime.getTime());
    }
    return null;
  }

}
