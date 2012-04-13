/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.transaction.managed;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.sql.Connection;
import java.util.Properties;

public class ManagedTransactionFactoryTest extends BaseDataTest {

  protected Mockery mockery = new Mockery() {
    {
      setImposteriser(ClassImposteriser.INSTANCE);
    }
  };

  protected final Connection conn = mockery.mock(Connection.class);

  @Test
  public void shouldEnsureThatCallsToManagedTransactionAPIDoNotForwardToManagedConnections() throws Exception {
    mockery.checking(new Expectations() {
      {
        one(conn).close();
      }
    });

    TransactionFactory tf = new ManagedTransactionFactory();
    tf.setProperties(new Properties());
    Transaction tx = tf.newTransaction(conn);
    assertEquals(conn, tx.getConnection());
    tx.commit();
    tx.rollback();
    tx.close();
    mockery.assertIsSatisfied();
  }


  @Test
  public void shouldEnsureThatCallsToManagedTransactionAPIDoNotForwardToManagedConnectionsAndDoesNotCloseConnection() throws Exception {
    mockery.checking(new Expectations() {
      {
        never(conn).close();
      }
    }); 

    TransactionFactory tf = new ManagedTransactionFactory();
    Properties props = new Properties();
    props.setProperty("closeConnection","false");
    tf.setProperties(props);
    Transaction tx = tf.newTransaction(conn);
    assertEquals(conn, tx.getConnection());
    tx.commit();
    tx.rollback();
    tx.close();
    mockery.assertIsSatisfied();
  }

}
