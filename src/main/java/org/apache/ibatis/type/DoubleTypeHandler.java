package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoubleTypeHandler extends BaseTypeHandler<Double> {

  public void setNonNullParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setDouble(i, parameter);
  }

  public Double getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getDouble(columnName);
  }

  public Double getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getDouble(columnIndex);
  }

}
