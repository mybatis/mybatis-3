/*
 *    Copyright 2009-2023 the original author or authors.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.Test;

class JdbcTransactionFactoryTest {

  @Test
  void testNullProperties() throws Exception {
    TestConnection connection = new TestConnection(false);
    JdbcTransactionFactory factory = new JdbcTransactionFactory();
    factory.setProperties(null);
    Transaction transaction = factory.newTransaction(connection);
    transaction.getConnection();
    transaction.close();
    assertTrue(connection.getAutoCommit());
  }

  @Test
  void testSkipSetAutoCommitOnClose() throws Exception {
    TestConnection connection = new TestConnection(false);
    DataSource ds = mock(DataSource.class);
    when(ds.getConnection()).thenReturn(connection);

    JdbcTransactionFactory factory = new JdbcTransactionFactory();
    Properties properties = new Properties();
    properties.setProperty("skipSetAutoCommitOnClose", "true");
    factory.setProperties(properties);
    Transaction transaction = factory.newTransaction(ds, TransactionIsolationLevel.NONE, false);
    transaction.getConnection();
    transaction.close();
    assertFalse(connection.getAutoCommit());
  }

}
