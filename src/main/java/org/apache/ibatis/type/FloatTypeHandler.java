package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatTypeHandler extends BaseTypeHandler<Float> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Float parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setFloat(i, parameter);
  }

  @Override
  public Float getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return rs.getFloat(columnName);
  }

  @Override
  public Float getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return rs.getFloat(columnIndex);
  }

  @Override
  public Float getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return cs.getFloat(columnIndex);
  }
}
