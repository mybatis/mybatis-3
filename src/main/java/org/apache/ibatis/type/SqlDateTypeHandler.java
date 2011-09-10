package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlDateTypeHandler extends BaseTypeHandler<Date> {

  public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setDate(i, parameter);
  }

  public Date getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getDate(columnName);
  }

  public Date getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getDate(columnIndex);
  }

}
