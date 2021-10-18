package org.apache.ibatis.type;


import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Type handler for subclasses of {@link EnumAlias}
 *
 * @author Konstantin Parakhin
 */
public class EnumAliasTypeHandler<TMnemonic extends Enum<TMnemonic> & EnumAlias> extends BaseTypeHandler<TMnemonic> {

  public static final String UNKNOWN_ENUM_NAME = "UNKNOWN";

  private final Class<TMnemonic> type;

  public EnumAliasTypeHandler(Class<TMnemonic> type) {
    this.type = type;
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, TMnemonic parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.getAlias());
  }

  @Override
  public TMnemonic getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String s = rs.getString(columnName);
    if (s == null && rs.wasNull()) {
      return null;
    }
    return asAlias(s);
  }

  @Override
  public TMnemonic getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String s = rs.getString(columnIndex);
    if (s == null && rs.wasNull()) {
      return null;
    }
    return asAlias(s);
  }

  @Override
  public TMnemonic getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String s = cs.getString(columnIndex);
    if (s == null && cs.wasNull()) {
      return null;
    }
    return asAlias(s);
  }

  private TMnemonic asAlias(final String value) {
    return Arrays.stream(type.getEnumConstants())
      .filter(x -> x.getAlias().equalsIgnoreCase(value))
      .findFirst()
      .orElse(null);
  }
}
