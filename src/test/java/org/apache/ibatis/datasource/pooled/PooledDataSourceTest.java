/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.datasource.pooled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PooledDataSourceTest {

  PooledDataSource dataSource;

  @BeforeEach
  void beforeEach() {
    dataSource = new PooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:multipledrivers", "sa", "");
  }

  @Test
  void shouldBlockUntilConnectionIsAvailableInPooledDataSource() throws Exception {
    dataSource.setPoolMaximumCheckoutTime(20000);

    List<Connection> connections = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(1);

    for (int i = 0; i < dataSource.getPoolMaximumActiveConnections(); i++) {
      connections.add(dataSource.getConnection());
    }

    new Thread(() -> {
      try {
        dataSource.getConnection();
        latch.countDown();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    assertFalse(latch.await(1000, TimeUnit.MILLISECONDS));
    connections.get(0).close();
    assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
  }

  @Test
  void PoppedConnectionShouldBeNotEqualToClosedConnection() throws Exception {
    Connection connectionToClose = dataSource.getConnection();
    CountDownLatch latch = new CountDownLatch(1);

    new Thread(() -> {
      try {
        latch.await();
        assertNotEquals(connectionToClose, dataSource.getConnection());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    connectionToClose.close();
    latch.countDown();
  }

  @Test
  void shouldEnsureCorrectIdleConnectionCount() throws Exception {
    dataSource.setPoolMaximumActiveConnections(10);
    dataSource.setPoolMaximumIdleConnections(5);

    PoolState poolState = dataSource.getPoolState();
    List<Connection> connections = new ArrayList<>();

    for (int i = 0; i < dataSource.getPoolMaximumActiveConnections(); i++) {
      connections.add(dataSource.getConnection());
    }

    assertEquals(0, poolState.getIdleConnectionCount());

    for (int i = 0; i < dataSource.getPoolMaximumActiveConnections(); i++) {
      connections.get(i).close();
    }

    assertEquals(dataSource.getPoolMaximumIdleConnections(), poolState.getIdleConnectionCount());

    for (int i = 0; i < dataSource.getPoolMaximumIdleConnections(); i++) {
      dataSource.getConnection();
    }

    assertEquals(0, poolState.getIdleConnectionCount());
  }

  @Test
  void connectionShouldBeAvailableAfterMaximumCheckoutTime() throws Exception {
    dataSource.setPoolMaximumCheckoutTime(1000);
    dataSource.setPoolTimeToWait(500);

    int poolMaximumActiveConnections = dataSource.getPoolMaximumActiveConnections();
    CountDownLatch latch = new CountDownLatch(1);

    for (int i = 0; i < poolMaximumActiveConnections; i++) {
      dataSource.getConnection();
    }

    new Thread(() -> {
      try {
        dataSource.getConnection();
        latch.countDown();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
  }

  @Test
  void forceCloseAllShouldRemoveAllActiveAndIdleConnection() throws SQLException {
    dataSource.setPoolMaximumActiveConnections(10);
    dataSource.setPoolMaximumIdleConnections(5);

    PoolState poolState = dataSource.getPoolState();
    List<Connection> connections = new ArrayList<>();

    for (int i = 0; i < dataSource.getPoolMaximumActiveConnections(); i++) {
      connections.add(dataSource.getConnection());
    }

    for (int i = 0; i < dataSource.getPoolMaximumIdleConnections(); i++) {
      connections.get(i).close();
    }

    assertEquals(dataSource.getPoolMaximumActiveConnections() - poolState.getIdleConnectionCount(),
        poolState.getActiveConnectionCount());
    assertEquals(dataSource.getPoolMaximumIdleConnections(), poolState.getIdleConnectionCount());

    dataSource.forceCloseAll();

    assertEquals(0, poolState.getActiveConnectionCount());
    assertEquals(0, poolState.getIdleConnectionCount());
  }

  @Test
  void shouldCloseConnectionOnRollbackFailureWhenPopping() throws Exception {
    java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean(false);
    Connection mockConnection = (Connection) java.lang.reflect.Proxy.newProxyInstance(
        PooledDataSourceTest.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
          if ("getAutoCommit".equals(method.getName())) {
            return false;
          }
          if ("rollback".equals(method.getName())) {
            throw new SQLException("Simulated rollback failure");
          }
          if ("close".equals(method.getName())) {
            closed.set(true);
            return null;
          }
          if ("isClosed".equals(method.getName())) {
            return closed.get();
          }
          if ("hashCode".equals(method.getName())) {
            return 12345;
          }
          if ("equals".equals(method.getName())) {
            return proxy == args[0];
          }
          return null;
        });

    UnpooledDataSource uds = new UnpooledDataSource() {
      @Override
      public Connection getConnection() throws SQLException {
        return mockConnection;
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return mockConnection;
      }
    };

    PooledDataSource pds = new PooledDataSource(uds);
    pds.setPoolMaximumActiveConnections(1);

    try {
      pds.getConnection();
    } catch (SQLException e) {
      // Expected
    }

    assertTrue(closed.get(), "Connection should have been closed after rollback failure");
  }

  @Test
  void shouldCloseConnectionOnRollbackFailureWhenPushing() throws Exception {
    java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean(false);
    java.util.concurrent.atomic.AtomicInteger rollbackCount = new java.util.concurrent.atomic.AtomicInteger(0);
    Connection mockConnection = (Connection) java.lang.reflect.Proxy.newProxyInstance(
        PooledDataSourceTest.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
          if ("getAutoCommit".equals(method.getName())) {
            return false;
          }
          if ("rollback".equals(method.getName())) {
            if (rollbackCount.incrementAndGet() > 1) {
              throw new SQLException("Simulated rollback failure");
            }
            return null;
          }
          if ("close".equals(method.getName())) {
            closed.set(true);
            return null;
          }
          if ("isClosed".equals(method.getName())) {
            return closed.get();
          }
          if ("hashCode".equals(method.getName())) {
            return 12345;
          }
          if ("equals".equals(method.getName())) {
            return proxy == args[0];
          }
          return null;
        });

    UnpooledDataSource uds = new UnpooledDataSource() {
      @Override
      public Connection getConnection() throws SQLException {
        return mockConnection;
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return mockConnection;
      }
    };

    PooledDataSource pds = new PooledDataSource(uds);
    pds.setPoolMaximumActiveConnections(1);

    Connection conn = pds.getConnection();
    try {
      conn.close();
    } catch (SQLException e) {
      // Expected
    }

    assertTrue(closed.get(), "Connection should have been closed after rollback failure on close/push");
  }
}
