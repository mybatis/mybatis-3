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
package org.apache.ibatis.transaction.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BooleanSupplier;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see JdbcTransaction
 */
class JdbcTransactionWithDataSourceTest extends JdbcTransactionBase {

  @Mock
  private DataSource dataSource;

  @Mock
  private Connection connection;

  @Mock
  private BooleanSupplier desiredAutoCommit;

  @Mock
  private BooleanSupplier skipSetAutoCommitClose;

  private Transaction transaction;

  @Test
  @Override
  void shouldGetConnection() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(desiredAutoCommit.getAsBoolean()).thenReturn(true);
    when(connection.getAutoCommit()).thenReturn(false);

    buildTransaction();
    Connection result = transaction.getConnection();

    assertEquals(connection, result);
    verify(dataSource).getConnection();
    verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    verify(connection).setAutoCommit(true);
  }

  @Test
  void shouldGetConnectionWithNotAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
    when(connection.getAutoCommit()).thenReturn(true);

    buildTransaction();
    Connection result = transaction.getConnection();

    assertEquals(connection, result);
    verify(dataSource).getConnection();
    verify(connection).setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    verify(connection).setAutoCommit(false);
  }

  @Test
  @Override
  void shouldCommitWhenConnectionIsNotAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getAutoCommit()).thenReturn(false);

    buildTransaction();
    transaction.getConnection();
    transaction.commit();

    verify(connection).commit();
  }

  @Test
  @Override
  void shouldAutoCommitWhenConnectionIsAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getAutoCommit()).thenReturn(true);

    buildTransaction();
    transaction.getConnection();
    transaction.commit();

    verify(connection, never()).commit();
  }

  @Test
  @Override
  void shouldRollbackWhenConnectionIsNotAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getAutoCommit()).thenReturn(false);

    buildTransaction();
    transaction.getConnection();
    transaction.rollback();

    verify(connection).rollback();
  }

  @Test
  @Override
  void shouldAutoRollbackWhenConnectionIsAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.getAutoCommit()).thenReturn(true);

    buildTransaction();
    transaction.getConnection();
    transaction.commit();

    verify(connection, never()).rollback();
  }

  @Test
  @Override
  void shouldCloseAndSetAutoCommitWhenConnectionIsNotAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
    when(skipSetAutoCommitClose.getAsBoolean()).thenReturn(false);

    buildTransaction();
    transaction.getConnection();
    transaction.close();

    verify(connection).close();
    verify(connection).setAutoCommit(true);
  }

  @Test
  @Override
  void shouldCloseAndNotSetAutoCommitWhenConnectionIsAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    when(desiredAutoCommit.getAsBoolean()).thenReturn(false);
    when(skipSetAutoCommitClose.getAsBoolean()).thenReturn(false);
    when(connection.getAutoCommit()).thenReturn(true);

    buildTransaction();
    transaction.getConnection();
    transaction.close();

    verify(connection).close();
    verify(connection, never()).setAutoCommit(true);
  }

  @Test
  @Override
  void shouldReturnNullWhenGetTimeout() throws SQLException {
    buildTransaction();

    assertNull(transaction.getTimeout());
  }

  private void buildTransaction() {
    this.transaction = new JdbcTransaction(dataSource, TransactionIsolationLevel.REPEATABLE_READ,
        desiredAutoCommit.getAsBoolean(), skipSetAutoCommitClose.getAsBoolean());
  }
}
