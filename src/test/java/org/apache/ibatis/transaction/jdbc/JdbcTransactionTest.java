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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.junit.jupiter.api.Test;

class JdbcTransactionTest {
  @Test
  void testSetAutoCommitOnClose() throws Exception {
    testAutoCommit(true, false, true, false);
    testAutoCommit(false, false, true, false);
    testAutoCommit(true, true, true, false);
    testAutoCommit(false, true, true, false);
    testAutoCommit(true, false, false, true);
    testAutoCommit(false, false, false, true);
    testAutoCommit(true, true, true, true);
    testAutoCommit(false, true, true, true);
  }

  private void testAutoCommit(boolean initialAutoCommit, boolean desiredAutoCommit, boolean resultAutoCommit,
      boolean skipSetAutoCommitOnClose) throws Exception {
    TestConnection con = new TestConnection(initialAutoCommit);
    DataSource ds = mock(DataSource.class);
    when(ds.getConnection()).thenReturn(con);

    JdbcTransaction transaction = new JdbcTransaction(ds, TransactionIsolationLevel.NONE, desiredAutoCommit,
        skipSetAutoCommitOnClose);
    transaction.getConnection();
    transaction.commit();
    transaction.close();

    assertEquals(resultAutoCommit, con.getAutoCommit());
  }
}
