/**
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Clinton Begin
 * @author umbum
 */
public class EnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

  private final Map<String, E> enumConstantDirectory;
  private final Field fieldAnnotatedByDbValue;
  private final Method methodAnnotatedByDbValue;

  public EnumTypeHandler(Class<E> type) {
    if (type == null) {
      throw new IllegalArgumentException("Type argument cannot be null");
    }

    E[] universe = type.getEnumConstants();
    if (universe == null) {
      throw new IllegalArgumentException(type.getSimpleName() + " is not an enum type");
    }

    List<Field> fieldsAnnotatedByDbValue = getAnnotatedMembers(type.getDeclaredFields());
    List<Method> methodsAnnotatedByDbValue = getAnnotatedMembers(type.getDeclaredMethods());

    if (methodsAnnotatedByDbValue.size() + fieldsAnnotatedByDbValue.size() > 1) {
      throw new IllegalArgumentException(type.getSimpleName() + ": Multiple '@DbValue' properties defined.");
    }

    fieldAnnotatedByDbValue =
      (fieldsAnnotatedByDbValue.size() == 1) ? fieldsAnnotatedByDbValue.get(0) : null;
    methodAnnotatedByDbValue =
      (methodsAnnotatedByDbValue.size() == 1) ? methodsAnnotatedByDbValue.get(0) : null;

    if (fieldAnnotatedByDbValue != null) {
      fieldAnnotatedByDbValue.setAccessible(true);
    }

    enumConstantDirectory = Arrays.stream(universe)
      .collect(Collectors.toMap(e -> getDbValue(e), e -> e));
  }

  private <T extends Member & AnnotatedElement> List<T> getAnnotatedMembers(T[] members) {
    return Arrays.stream(members)
      .filter(field -> field.isAnnotationPresent(DbValue.class))
      .collect(Collectors.toList());
  }

  private String getDbValue(E e) {
    try {
      return fieldAnnotatedByDbValue != null ? (String)fieldAnnotatedByDbValue.get(e)
        : methodAnnotatedByDbValue != null ? (String)methodAnnotatedByDbValue.invoke(e)
        : e.name();
    } catch (IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalStateException("Unexpected reflection exception", ex);
    }
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
    if (jdbcType == null) {
      ps.setString(i, getDbValue(parameter));
    } else {
      ps.setObject(i, getDbValue(parameter), jdbcType.TYPE_CODE); // see r3589
    }
  }

  @Override
  public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String s = rs.getString(columnName);
    return s == null ? null : enumConstantDirectory.get(s);
  }

  @Override
  public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String s = rs.getString(columnIndex);
    return s == null ? null : enumConstantDirectory.get(s);
  }

  @Override
  public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String s = cs.getString(columnIndex);
    return s == null ? null : enumConstantDirectory.get(s);
  }
}
