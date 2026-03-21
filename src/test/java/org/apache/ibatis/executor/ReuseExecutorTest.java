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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.Test;

class ReuseExecutorTest extends BaseExecutorTest {

  @Test
  void dummy() {
  }

  @Test
  void shouldNotReuseClosedStatement() throws Exception {
    ReuseExecutor executor = new ReuseExecutor(config, mock(Transaction.class));
    Connection connection = mock(Connection.class);
    Statement statement = mock(Statement.class);
    when(statement.isClosed()).thenReturn(true);
    when(statement.getConnection()).thenReturn(connection);
    when(connection.isClosed()).thenReturn(false);

    Field statementMapField = ReuseExecutor.class.getDeclaredField("statementMap");
    statementMapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Statement> statementMap = (Map<String, Statement>) statementMapField.get(executor);
    statementMap.put("SELECT 1", statement);

    Method hasStatementFor = ReuseExecutor.class.getDeclaredMethod("hasStatementFor", String.class);
    hasStatementFor.setAccessible(true);

    boolean result = (boolean) hasStatementFor.invoke(executor, "SELECT 1");

    assertFalse(result);
    verify(statement).isClosed();
    verify(statement, never()).getConnection();
  }

  @Test
  void shouldPrepareNewStatementWhenCachedStatementIsClosed() throws Exception {
    Transaction transaction = mock(Transaction.class);
    ReuseExecutor executor = new ReuseExecutor(config, transaction);
    Connection connection = mock(Connection.class);
    Log log = mock(Log.class);
    StatementHandler handler = mock(StatementHandler.class);
    BoundSql boundSql = mock(BoundSql.class);
    Statement closedStatement = mock(Statement.class);
    Statement newStatement = mock(Statement.class);

    when(log.isDebugEnabled()).thenReturn(false);
    when(transaction.getConnection()).thenReturn(connection);
    when(transaction.getTimeout()).thenReturn(0);
    when(handler.getBoundSql()).thenReturn(boundSql);
    when(boundSql.getSql()).thenReturn("SELECT 1");
    when(closedStatement.isClosed()).thenReturn(true);
    when(closedStatement.getConnection()).thenReturn(connection);
    when(connection.isClosed()).thenReturn(false);
    when(handler.prepare(connection, 0)).thenReturn(newStatement);

    Field statementMapField = ReuseExecutor.class.getDeclaredField("statementMap");
    statementMapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Statement> statementMap = (Map<String, Statement>) statementMapField.get(executor);
    statementMap.put("SELECT 1", closedStatement);

    Method prepareStatement = ReuseExecutor.class.getDeclaredMethod("prepareStatement", StatementHandler.class,
        Log.class);
    prepareStatement.setAccessible(true);

    Statement result = (Statement) prepareStatement.invoke(executor, handler, log);

    assertSame(newStatement, result);
    verify(closedStatement).isClosed();
    verify(closedStatement, never()).getConnection();
    verify(handler).prepare(connection, 0);
    verify(handler).parameterize(newStatement);
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
