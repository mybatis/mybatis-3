package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DateTypeHandler extends BaseTypeHandler<Date> {

  public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, new java.sql.Timestamp((parameter).getTime()));
  }

  public Date getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    java.sql.Timestamp sqlTimestamp = rs.getTimestamp(columnName);
    if (sqlTimestamp != null) {
      return new java.util.Date(sqlTimestamp.getTime());
    }
    return null;
  }

  public Date getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    java.sql.Timestamp sqlTimestamp = cs.getTimestamp(columnIndex);
    if (sqlTimestamp != null) {
      return new java.util.Date(sqlTimestamp.getTime());
    }
    return null;
  }

}
