package org.apache.ibatis.submitted.generictypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class UserTypeHandler extends BaseTypeHandler<User<String>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, User<String> parameter, JdbcType jdbcType) throws SQLException {
  }

  @Override
  public User<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return new User<String>();
  }

  @Override
  public User<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return new User<String>();
  }

  @Override
  public User<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return new User<String>();
  }

}
