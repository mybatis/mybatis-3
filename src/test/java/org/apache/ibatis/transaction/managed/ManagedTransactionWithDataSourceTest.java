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
package org.apache.ibatis.transaction.managed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see ManagedTransaction
 */
class ManagedTransactionWithDataSourceTest extends ManagedTransactionBase {

  @Mock
  private DataSource dataSource;

  @Mock
  private Connection connection;

  private Transaction transaction;

  @BeforeEach
  void setup() {
    this.transaction = new ManagedTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, true);
  }

  @Override
  @Test
  void shouldGetConnection() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);

    Connection result = transaction.getConnection();

    assertEquals(connection, result);
    verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
  }

  @Test
  @Override
  void shouldNotCommitWhetherConnectionIsAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);

    transaction.getConnection();
    transaction.commit();

    verify(connection, never()).commit();
    verify(connection, never()).getAutoCommit();
  }

  @Test
  @Override
  void shouldNotRollbackWhetherConnectionIsAutoCommit() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);

    transaction.getConnection();
    transaction.rollback();

    verify(connection, never()).rollback();
    verify(connection, never()).getAutoCommit();
  }

  @Test
  @Override
  void shouldCloseWhenSetCloseConnectionIsTrue() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);

    transaction.getConnection();
    transaction.close();

    verify(connection).close();
  }

  @Test
  @Override
  void shouldNotCloseWhenSetCloseConnectionIsFalse() throws SQLException {
    transaction = new ManagedTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, false);
    when(dataSource.getConnection()).thenReturn(connection);

    transaction.getConnection();
    transaction.close();

    verify(connection, never()).close();
  }

  @Test
  @Override
  void shouldReturnNullWhenGetTimeout() throws SQLException {
    assertNull(transaction.getTimeout());
  }
}
