package org.apache.ibatis.type;

import java.sql.*;
import java.time.ZoneId;

/**
 * @author Adrián Boimvaser
 */
public class ZoneIdTypeHandler extends BaseTypeHandler<ZoneId> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, ZoneId parameter, JdbcType jdbcType)
    throws SQLException {
    ps.setString(i, parameter.getId());
  }

  @Override
  public ZoneId getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String s = rs.getString(columnName);
    return s == null ? null : ZoneId.of(s);
  }

  @Override
  public ZoneId getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String s = rs.getString(columnIndex);
    return s == null ? null : ZoneId.of(s);
  }

  @Override
  public ZoneId getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String s = cs.getString(columnIndex);
    return s == null ? null : ZoneId.of(s);
  }
}
