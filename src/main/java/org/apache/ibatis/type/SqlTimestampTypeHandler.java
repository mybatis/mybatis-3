package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SqlTimestampTypeHandler extends BaseTypeHandler<Timestamp> {

  public void setNonNullParameter(PreparedStatement ps, int i, Timestamp parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setTimestamp(i, parameter);
  }

  public Timestamp getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getTimestamp(columnName);
  }

  public Timestamp getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getTimestamp(columnIndex);
  }

}
