package org.apache.ibatis.type;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @description: Core method --> {@link TypeHandlerRegistry#register(Type, JdbcType, TypeHandler)}
 * When we have more than one enumeration in the mapper.xml, the {@link TypeHandlerRegistry#allTypeHandlersMap}
 * will be covered.
 * @author: zhangzhenwei
 * @date: 2021/10/24 21:55
 */
public class EnumTypeAdapterHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

  private Class<E> type = null;
  private E[] enums = null;

  public EnumTypeAdapterHandler(Class<E> type) {
    if (type == null) {
      throw new IllegalArgumentException("Type argument cannot be null");
    }
    this.type = type;
    this.enums = type.getEnumConstants();
  }

  public EnumTypeAdapterHandler() {
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
    if (jdbcType == null) {
      ps.setString(i, parameter.name());
    } else {
      ps.setObject(i, parameter.name(), jdbcType.TYPE_CODE); // see r3589
    }
  }

  @Override
  public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
    if (enums == null) {return null;}
    String result = rs.getString(columnName);
    return Arrays.stream(enums).filter(anEnum -> anEnum.name().equals(result)).findFirst().orElse(null);
  }

  @Override
  public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    if (enums == null) {return null;}
    String result = rs.getString(columnIndex);
    return Arrays.stream(enums).filter(anEnum -> anEnum.name().equals(result)).findFirst().orElse(null);
  }

  @Override
  public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    if (enums == null) {return null;}
    String result = cs.getString(columnIndex);
    return Arrays.stream(enums).filter(anEnum -> anEnum.name().equals(result)).findFirst().orElse(null);
  }



}
