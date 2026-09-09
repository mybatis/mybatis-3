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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reproduces the race condition described in <a href="https://github.com/mybatis/mybatis-3/issues/3524">#3524</a>.
 * <p>
 * When {@code poolMaximumActiveConnections = 1}, after thread2 claims an overdue connection, thread1 returns its
 * invalidated connection and {@code pushConnection()} incorrectly removes thread2's connection because
 * {@link PooledConnection#equals} compares by {@code realConnection.hashCode()}. This causes the pool to believe
 * activeConnections is 0, allowing thread3 to create a second physical connection — violating the configured maximum.
 */
class PooledDataSourceConcurrencyTest {

  private PooledDataSource dataSource;

  @BeforeEach
  void setUp() {
    dataSource = new PooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:issue3524", "sa", "");
    // Strictly limit to a single active connection
    dataSource.setPoolMaximumActiveConnections(1);
    dataSource.setPoolMaximumIdleConnections(1);
    // Use short timeouts to speed up the test
    dataSource.setPoolMaximumCheckoutTime(1000); // 1-second checkout timeout
    dataSource.setPoolTimeToWait(500); // 0.5-second retry wait
    dataSource.setPoolPingEnabled(false);
  }

  /**
   * Simulates a full three-thread scenario to verify whether physical connections exceeding the limit are created.
   * <p>
   * Detection approach: after thread2 claims thread1's overdue connection, both share the same realConnection (RC1).
   * Thread2 exposes its unwrapped realConnection reference so that thread3 can compare: if thread3 obtains a different
   * physical connection object (RC2 != RC1), the pool limit has been violated.
   * <p>
   * Reproduction steps:
   * <ol>
   * <li>thread1 acquires the only connection (PC1 wrapping RC1) and holds it past the checkout timeout.</li>
   * <li>thread2 waits, detects PC1 as overdue, invalidates PC1, and creates a new PC2 wrapping the same RC1 in
   * activeConnections.</li>
   * <li>thread1 finishes and calls close() → pushConnection(PC1). Because PC1.equals(PC2) is true (same underlying
   * realConnection), remove(PC1) incorrectly removes PC2 from activeConnections.</li>
   * <li>thread3 finds activeConnections empty and creates a brand-new physical connection RC2.</li>
   * <li>thread3 compares its RC2 with thread2's RC1: if they are different objects, two physical connections exist
   * simultaneously.</li>
   * </ol>
   * <p>
   * Expected result: if the bug exists, the test FAILs (thread2 and thread3 hold different physical connections).
   */
  @Test
  void shouldNotExceedMaxActiveConnectionsUnderContention() throws Exception {
    // thread2 shares its unwrapped realConnection for thread3 to compare
    AtomicReference<Connection> thread2RealConnRef = new AtomicReference<>();
    AtomicReference<String> violation = new AtomicReference<>();

    // Phase gates for orchestrating precise thread interleaving
    CountDownLatch thread1Acquired = new CountDownLatch(1); // thread1 has acquired a connection
    CountDownLatch thread2Claimed = new CountDownLatch(1); // thread2 has claimed the overdue connection
    CountDownLatch thread1Returned = new CountDownLatch(1); // thread1 has returned its connection
    CountDownLatch thread3Checked = new CountDownLatch(1); // thread3 has completed its check
    CountDownLatch allDone = new CountDownLatch(3); // all threads have finished

    // ---- thread1: acquire a connection and hold it until timeout ----
    Thread t1 = new Thread(() -> {
      try {
        // Acquire connection; at this point activeConnections = [PC1(RC1)]
        Connection conn1 = dataSource.getConnection();

        thread1Acquired.countDown();

        // Wait for thread2 to claim our overdue connection
        thread2Claimed.await(10, TimeUnit.SECONDS);

        // Return the connection — triggers pushConnection(PC1)
        // BUG: since PC1.equals(PC2) == true, this incorrectly removes thread2's PC2 from activeConnections
        conn1.close();

        thread1Returned.countDown();
      } catch (Exception e) {
        thread1Returned.countDown();
      } finally {
        allDone.countDown();
      }
    }, "thread1-holder");

    // ---- thread2: wait for checkout timeout and claim the overdue connection ----
    Thread t2 = new Thread(() -> {
      try {
        thread1Acquired.await(5, TimeUnit.SECONDS);

        // Blocks until checkout timeout, then claims thread1's overdue connection (still the same RC1)
        Connection conn2 = dataSource.getConnection();

        // Unwrap and share the realConnection reference for thread3 to compare
        Connection real2 = PooledDataSource.unwrapConnection(conn2);
        thread2RealConnRef.set(real2);

        thread2Claimed.countDown();

        // Wait for thread3 to finish checking before releasing, ensuring thread2 and thread3 hold connections
        // simultaneously
        thread3Checked.await(10, TimeUnit.SECONDS);

        // thread2 releases its connection
        conn2.close();
      } catch (Exception e) {
        thread2Claimed.countDown();
      } finally {
        allDone.countDown();
      }
    }, "thread2-claimer");

    // ---- thread3: attempt to acquire a connection after thread1 returns ----
    Thread t3 = new Thread(() -> {
      try {
        thread1Returned.await(10, TimeUnit.SECONDS);
        Thread.sleep(100); // Ensure pushConnection has completed

        // If the bug exists: activeConnections is empty (PC2 was incorrectly removed),
        // so the pool creates a brand-new physical connection (RC2) — a second physical connection.
        Connection conn3 = dataSource.getConnection();

        // Unwrap thread3's realConnection
        Connection real3 = PooledDataSource.unwrapConnection(conn3);
        // Get thread2's shared realConnection
        Connection real2 = thread2RealConnRef.get();

        // [Core detection] Compare whether the two realConnections are the same object
        // If the bug exists: real3 != real2 (two different physical connections exist simultaneously)
        // If the bug is fixed: thread3 should get the same connection returned by thread2, or wait for thread2
        if (real2 != null && real3 != real2) {
          violation.set("BUG REPRODUCED: thread2's realConnection (identityHashCode=" + System.identityHashCode(real2)
              + ") and thread3's realConnection (identityHashCode=" + System.identityHashCode(real3)
              + ") are different physical connection objects! "
              + "The poolMaximumActiveConnections=1 constraint has been violated — two physical connections exist simultaneously.");
        }

        thread3Checked.countDown();
        conn3.close();
      } catch (Exception e) {
        thread3Checked.countDown();
      } finally {
        allDone.countDown();
      }
    }, "thread3-newcomer");

    t1.start();
    t2.start();
    t3.start();

    if (!allDone.await(30, TimeUnit.SECONDS)) {
      fail("Test timed out — threads did not finish within 30 seconds");
    }

    if (violation.get() != null) {
      fail(violation.get());
    }
  }

  /**
   * Deterministic test: directly inspects pool state to prove the bug exists.
   * <p>
   * After thread2 claims the overdue connection, thread1 returns its invalidated connection. At that point
   * activeConnections should still be 1 (thread2 is still using the connection). However, because
   * PooledConnection.equals() compares by realConnection.hashCode(), pushConnection(PC1) incorrectly removes PC2 from
   * activeConnections, causing activeConnections to become 0.
   * <p>
   * Reproduction steps:
   * <ol>
   * <li>The main thread (acting as thread1) acquires the only connection conn1 (PC1 wrapping RC1),
   * activeConnections = [PC1].</li>
   * <li>thread2 is started and calls getConnection(). Since the pool is full, thread2 blocks.</li>
   * <li>After poolMaximumCheckoutTime (1 second) elapses, thread2 marks PC1 as invalid and creates a new PC2 wrapping
   * the same RC1. Now activeConnections = [PC2].</li>
   * <li>The main thread calls conn1.close(), triggering pushConnection(PC1).</li>
   * <li>Inside pushConnection, activeConnections.remove(PC1) is called. Because PC1.equals(PC2) == true (both wrap the
   * same realConnection), this incorrectly removes PC2 from activeConnections.</li>
   * <li>Now activeConnections = [], idleConnections = [], yet thread2 still holds a connection! The pool state is
   * inconsistent with reality.</li>
   * </ol>
   * <p>
   * Expected result: if the bug exists, the test FAILs with "BUG REPRODUCED".
   */
  @Test
  void activeConnectionsShouldNotBecomeInconsistentAfterOverdueClaim() throws Exception {
    // Main thread (thread1) acquires and holds a connection
    Connection conn1 = dataSource.getConnection();
    PoolState poolState = dataSource.getPoolState();

    // Verify initial state: 1 active connection, 0 idle connections
    assertPoolState(poolState, 1, 0, "after thread1 acquires connection");

    // thread2 will claim the overdue connection after checkout timeout
    CountDownLatch thread2Ready = new CountDownLatch(1);
    AtomicReference<Connection> conn2Ref = new AtomicReference<>();

    Thread thread2 = new Thread(() -> {
      try {
        // Blocks until checkout timeout, then claims the overdue connection
        Connection c = dataSource.getConnection();
        conn2Ref.set(c);
        thread2Ready.countDown();
      } catch (Exception e) {
        thread2Ready.countDown();
      }
    }, "thread2");
    thread2.start();

    // Wait for thread2 to successfully claim the overdue connection
    thread2Ready.await(10, TimeUnit.SECONDS);

    // At this point thread2 has claimed the connection; pool state should be: 1 active (PC2), 0 idle
    assertPoolState(poolState, 1, 0, "after thread2 claims overdue connection");

    // Main thread (thread1) returns its invalidated connection — this is where the bug triggers
    conn1.close();

    // [BUG DETECTION POINT]
    // After pushConnection(PC1), activeConnections should still be 1 (thread2 is still using PC2).
    // But due to the equals() bug, PC1's remove operation incorrectly removes PC2, causing activeConnections = 0.
    int activeAfterReturn = poolState.getActiveConnectionCount();
    int idleAfterReturn = poolState.getIdleConnectionCount();

    // Clean up thread2's connection
    Connection c2 = conn2Ref.get();
    if (c2 != null) {
      c2.close();
    }
    thread2.join(5000);

    // If both activeConnections and idleConnections are 0, the bug has been reproduced:
    // thread2's connection has been "forgotten" by the pool, and subsequent threads can create
    // additional physical connections, violating the configured limit.
    if (activeAfterReturn == 0 && idleAfterReturn == 0) {
      fail("BUG REPRODUCED: after thread1 returns its invalidated connection, activeConnections became 0, "
          + "yet thread2 still holds a connection. The cause is that pushConnection(PC1) incorrectly removed "
          + "thread2's PC2 because PooledConnection.equals() compares by realConnection.hashCode(). "
          + "(activeConnections=" + activeAfterReturn + ", idleConnections=" + idleAfterReturn + ")");
    }
  }

  private void assertPoolState(PoolState state, int expectedActive, int expectedIdle, String phase) {
    int active = state.getActiveConnectionCount();
    int idle = state.getIdleConnectionCount();
    if (active != expectedActive || idle != expectedIdle) {
      fail("Pool state inconsistency [" + phase + "]: expected active=" + expectedActive + ", idle=" + expectedIdle
          + ", but got active=" + active + ", idle=" + idle);
    }
  }
}
