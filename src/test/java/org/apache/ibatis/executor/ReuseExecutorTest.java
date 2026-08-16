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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.Test;

class ReuseExecutorTest extends BaseExecutorTest {

  @Test
  void dummy() {
  }

  @Test
  void shouldPrepareNewStatementWhenCachedStatementIsClosed() throws SQLException {
    Transaction transaction = mock(Transaction.class);
    Connection connection = mock(Connection.class);
    PreparedStatement closedStatement = mock(PreparedStatement.class);
    PreparedStatement newStatement = mock(PreparedStatement.class);
    String sql = "UPDATE author SET username = 'someone' WHERE id = 101";
    MappedStatement ms = new MappedStatement.Builder(config, "updateAuthorForReuseExecutor",
        new StaticSqlSource(config, sql), SqlCommandType.UPDATE).build();
    ReuseExecutor executor = new ReuseExecutor(config, transaction);

    when(transaction.getConnection()).thenReturn(connection);
    when(transaction.getTimeout()).thenReturn(null);
    when(connection.prepareStatement(sql)).thenReturn(closedStatement, newStatement);
    when(connection.isClosed()).thenReturn(false);
    when(closedStatement.isClosed()).thenReturn(true);
    when(closedStatement.getConnection()).thenReturn(connection);
    when(closedStatement.execute()).thenReturn(false).thenThrow(new SQLException("Statement is closed."));
    when(closedStatement.getUpdateCount()).thenReturn(1);
    when(newStatement.execute()).thenReturn(false);
    when(newStatement.getUpdateCount()).thenReturn(1);

    assertDoesNotThrow(() -> {
      executor.update(ms, null);
      executor.update(ms, null);
    });

    verify(connection, times(2)).prepareStatement(sql);
    verify(closedStatement).isClosed();
    verify(closedStatement, never()).getConnection();
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
