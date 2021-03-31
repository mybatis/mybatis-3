package org.apache.ibatis.builder.typehandler;

import org.apache.ibatis.autoconstructor.BadSubject;
import org.apache.ibatis.executor.result.ResultClassTypeHolder;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.Assert;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WidthTypeHandler extends BaseTypeHandler<BadSubject.Width> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, BadSubject.Width parameter, JdbcType jdbcType) throws SQLException {

  }

  @Override
  public BadSubject.Width getNullableResult(ResultSet rs, String columnName) throws SQLException {
    Assert.assertNotNull(ResultClassTypeHolder.getResultType());
    return new BadSubject.Width();
  }

  @Override
  public BadSubject.Width getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return new BadSubject.Width();
  }

  @Override
  public BadSubject.Width getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return new BadSubject.Width();
  }
}
