/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.ExecutorException;

public class ConflictedTypeHandler implements TypeHandler<Object> {

  private final Class<?> javaType;
  private final JdbcType jdbcType;
  private final Set<TypeHandler<?>> conflictedTypeHandlers = new HashSet<>();

  public ConflictedTypeHandler(Class<?> javaType, JdbcType jdbcType, TypeHandler<?> existing, TypeHandler<?> added) {
    super();
    this.javaType = javaType;
    this.jdbcType = jdbcType;
    if (existing instanceof ConflictedTypeHandler) {
      conflictedTypeHandlers.addAll(((ConflictedTypeHandler) existing).getConflictedTypeHandlers());
    } else {
      conflictedTypeHandlers.add(existing);
    }
    conflictedTypeHandlers.add(added);
  }

  private Set<TypeHandler<?>> getConflictedTypeHandlers() {
    return conflictedTypeHandlers;
  }

  @Override
  public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
    throw exception();
  }

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    throw exception();
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    throw exception();
  }

  @Override
  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    throw exception();
  }

  private ExecutorException exception() {
    return new ExecutorException(
        "Multiple type-aware handlers are registered and being looked up without type; javaType=" + javaType
            + ", jdbcType=" + jdbcType + ", type handlers="
            + conflictedTypeHandlers.stream().map(x -> x.getClass().getName()).collect(Collectors.joining(",")));
  }
}
