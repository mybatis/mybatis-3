package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShortTypeHandler extends BaseTypeHandler<Short> {

  public void setNonNullParameter(PreparedStatement ps, int i, Short parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setShort(i, parameter);
  }

  public Short getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getShort(columnName);
  }

  public Short getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getShort(columnIndex);
  }

}
