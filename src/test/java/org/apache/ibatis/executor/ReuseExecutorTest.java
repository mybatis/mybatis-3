/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

class ReuseExecutorTest extends BaseExecutorTest {

  @Test
  void shouldReuseStatementWithDifferentParameters() throws Exception {
    Transaction transaction = mock(Transaction.class);
    Connection connection = mock(Connection.class);
    PreparedStatement statement = mock(PreparedStatement.class);
    String sql = "update items set name = ?";
    when(transaction.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(sql)).thenReturn(statement);
    when(statement.getConnection()).thenReturn(connection);
    MappedStatement ms = new MappedStatement.Builder(config, "updateItem",
        new StaticSqlSource(config, sql,
            List.of(new ParameterMapping.Builder(config, "name", String.class).jdbcType(JdbcType.VARCHAR).build())),
        SqlCommandType.UPDATE).build();
    ReuseExecutor executor = new ReuseExecutor(config, transaction);
    try {
      executor.update(ms, "first");
      executor.update(ms, "second");
      verify(connection).prepareStatement(sql);
      verify(statement).setString(1, "first");
      verify(statement).setString(1, "second");
    } finally {
      executor.close(false);
    }
    verify(statement).close();
  }

  @Test
  void shouldPrepareDifferentSqlForTheSameMappedStatement() throws Exception {
    Transaction transaction = mock(Transaction.class);
    Connection connection = mock(Connection.class);
    PreparedStatement first = mock(PreparedStatement.class);
    PreparedStatement second = mock(PreparedStatement.class);
    when(transaction.getConnection()).thenReturn(connection);
    when(connection.prepareStatement("delete from items")).thenReturn(first);
    when(connection.prepareStatement("delete from items  where id = ?")).thenReturn(second);
    MappedStatement ms = new MappedStatement.Builder(config, "deleteItem",
        new XMLLanguageDriver().createSqlSource(config,
            "<script>delete from items <if test='_parameter != null'>where id = #{id,jdbcType=INTEGER}</if></script>",
            Integer.class),
        SqlCommandType.DELETE).build();
    ReuseExecutor executor = new ReuseExecutor(config, transaction);
    try {
      executor.update(ms, null);
      executor.update(ms, 1);
      verify(first).execute();
      verify(second).execute();
      verify(second).setInt(1, 1);
    } finally {
      executor.close(false);
    }
    verify(first).close();
    verify(second).close();
  }

  @Test
  void shouldCloseAllMappedStatementsOnFlushAndPrepareAgain() throws Exception {
    Transaction transaction = mock(Transaction.class);
    Connection connection = mock(Connection.class);
    PreparedStatement first = mock(PreparedStatement.class);
    PreparedStatement second = mock(PreparedStatement.class);
    PreparedStatement afterFlush = mock(PreparedStatement.class);
    String sql = "delete from items";
    when(transaction.getConnection()).thenReturn(connection);
    when(connection.prepareStatement(sql)).thenReturn(first, second, afterFlush);
    when(first.getConnection()).thenReturn(connection);
    MappedStatement firstMs = new MappedStatement.Builder(config, "deleteFirst", new StaticSqlSource(config, sql),
        SqlCommandType.DELETE).build();
    MappedStatement secondMs = new MappedStatement.Builder(config, "deleteSecond", new StaticSqlSource(config, sql),
        SqlCommandType.DELETE).build();
    ReuseExecutor executor = new ReuseExecutor(config, transaction);
    try {
      executor.update(firstMs, null);
      executor.update(secondMs, null);
      executor.flushStatements();
      verify(first).close();
      verify(second).close();
      executor.update(firstMs, null);
      verify(connection, times(3)).prepareStatement(sql);
      verify(afterFlush).execute();
    } finally {
      executor.close(false);
    }
    verify(afterFlush).close();
  }

  @Test
  void dummy() {
  }

  @Override
  @Test
  public void shouldFetchPostWithBlogWithCompositeKey() {
    assertDoesNotThrow(super::shouldFetchPostWithBlogWithCompositeKey);
  }

  @Override
  protected Executor createExecutor(Transaction transaction) {
    return new ReuseExecutor(config, transaction);
  }
}
