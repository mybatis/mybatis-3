package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {

  public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setBoolean(i, parameter);
  }

  public Boolean getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getBoolean(columnName);
  }

  public Boolean getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getBoolean(columnIndex);
  }


}
