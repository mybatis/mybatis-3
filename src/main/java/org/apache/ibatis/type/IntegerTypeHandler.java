package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeHandler extends BaseTypeHandler<Integer> {

  public void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setInt(i, parameter);
  }

  public Integer getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getInt(columnName);
  }

  public Integer getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getInt(columnIndex);
  }

}
