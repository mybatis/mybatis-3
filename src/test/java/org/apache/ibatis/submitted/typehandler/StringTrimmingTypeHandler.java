package org.apache.ibatis.submitted.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedTypes(String.class)
@MappedJdbcTypes(value={JdbcType.CHAR,JdbcType.VARCHAR}, includeNullJdbcType=true)
public class StringTrimmingTypeHandler implements TypeHandler<String> {

  @Override
  public void setParameter(PreparedStatement ps, int i, String parameter,
      JdbcType jdbcType) throws SQLException {
    ps.setString(i, trim(parameter));
  }

  @Override
  public String getResult(ResultSet rs, String columnName) throws SQLException {
    return trim(rs.getString(columnName));
  }

  @Override
  public String getResult(ResultSet rs, int columnIndex) throws SQLException {
    return trim(rs.getString(columnIndex));
  }

  @Override
  public String getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return trim(cs.getString(columnIndex));
  }

  private String trim(String s) {
    if (s == null) {
      return null;
    } else {
      return s.trim();
    }
  }
}
