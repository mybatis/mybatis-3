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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.TransactionFactoryBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see JdbcTransactionFactory
 */
class JdbcTransactionFactoryUnitTest extends TransactionFactoryBase {

  @Mock
  private Properties properties;

  @Mock
  private Connection connection;

  @Mock
  private DataSource dataSource;

  private TransactionFactory transactionFactory;

  @BeforeEach
  void setup() {
    this.transactionFactory = new JdbcTransactionFactory();
  }

  @Test
  @Override
  public void shouldSetProperties() throws Exception {
    when(properties.getProperty("skipSetAutoCommitOnClose")).thenReturn("true");

    transactionFactory.setProperties(properties);

    assertTrue((Boolean) getValue(transactionFactory.getClass().getDeclaredField("skipSetAutoCommitOnClose"),
        transactionFactory));
  }

  @Test
  @Override
  public void shouldNewTransactionWithConnection() throws SQLException {
    Transaction result = transactionFactory.newTransaction(connection);

    assertNotNull(result);
    assertInstanceOf(JdbcTransaction.class, result);
    assertEquals(connection, result.getConnection());
  }

  @Test
  @Override
  public void shouldNewTransactionWithDataSource() throws Exception {
    when(dataSource.getConnection()).thenReturn(connection);

    Transaction result = transactionFactory.newTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, false);

    assertNotNull(result);
    assertInstanceOf(JdbcTransaction.class, result);
    assertEquals(connection, result.getConnection());
    verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

    assertEquals(dataSource, getValue(result.getClass().getDeclaredField("dataSource"), result));
    assertEquals(TransactionIsolationLevel.READ_COMMITTED,
        getValue(result.getClass().getDeclaredField("level"), result));
    assertEquals(false, getValue(result.getClass().getDeclaredField("autoCommit"), result));
    assertEquals(false, getValue(result.getClass().getDeclaredField("skipSetAutoCommitOnClose"), result));
  }

  @Test
  void shouldNewTransactionWithDataSourceAndCustomProperties() throws Exception {
    when(dataSource.getConnection()).thenReturn(connection);
    when(properties.getProperty("skipSetAutoCommitOnClose")).thenReturn("true");

    transactionFactory.setProperties(properties);
    Transaction result = transactionFactory.newTransaction(dataSource, TransactionIsolationLevel.READ_COMMITTED, true);

    assertNotNull(result);
    assertInstanceOf(JdbcTransaction.class, result);
    assertEquals(connection, result.getConnection());
    verify(connection).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

    assertEquals(dataSource, getValue(result.getClass().getDeclaredField("dataSource"), result));
    assertEquals(TransactionIsolationLevel.READ_COMMITTED,
        getValue(result.getClass().getDeclaredField("level"), result));
    assertEquals(true, getValue(result.getClass().getDeclaredField("autoCommit"), result));
    assertEquals(true, getValue(result.getClass().getDeclaredField("skipSetAutoCommitOnClose"), result));
  }

}
