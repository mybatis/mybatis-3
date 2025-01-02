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

import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see JdbcTransaction
 */
class JdbcTransactionWithConnectionTest extends JdbcTransactionBase {

  @Mock
  private Connection connection;

  private Transaction transaction;

  @BeforeEach
  void setup() {
    this.transaction = new JdbcTransaction(connection);
  }

  @Test
  @Override
  void shouldGetConnection() throws SQLException {
    Connection result = transaction.getConnection();

    assertEquals(connection, result);
  }

  @Test
  @Override
  void shouldCommitWhenConnectionIsNotAutoCommit() throws SQLException {
    when(connection.getAutoCommit()).thenReturn(false);

    transaction.commit();

    verify(connection).commit();
    verify(connection).getAutoCommit();
  }

  @Test
  @Override
  void shouldAutoCommitWhenConnectionIsAutoCommit() throws SQLException {
    when(connection.getAutoCommit()).thenReturn(true);

    transaction.commit();

    verify(connection, never()).commit();
    verify(connection).getAutoCommit();
  }

  @Test
  @Override
  void shouldRollbackWhenConnectionIsNotAutoCommit() throws SQLException {
    when(connection.getAutoCommit()).thenReturn(false);

    transaction.rollback();

    verify(connection).rollback();
    verify(connection).getAutoCommit();
  }

  @Test
  @Override
  void shouldAutoRollbackWhenConnectionIsAutoCommit() throws SQLException {
    when(connection.getAutoCommit()).thenReturn(true);

    transaction.rollback();

    verify(connection, never()).rollback();
    verify(connection).getAutoCommit();
  }

  @Test
  @Override
  void shouldCloseAndSetAutoCommitWhenConnectionIsNotAutoCommit() throws SQLException {
    when(connection.getAutoCommit()).thenReturn(false);

    transaction.close();

    verify(connection).close();
    verify(connection).setAutoCommit(true);
    verify(connection).getAutoCommit();
  }

  @Test
  @Override
  void shouldCloseAndNotSetAutoCommitWhenConnectionIsAutoCommit() throws SQLException {
    when(connection.getAutoCommit()).thenReturn(true);

    transaction.close();

    verify(connection).close();
    verify(connection, never()).setAutoCommit(true);
    verify(connection).getAutoCommit();
  }

  @Test
  @Override
  void shouldReturnNullWhenGetTimeout() throws SQLException {
    assertNull(transaction.getTimeout());
  }

}
