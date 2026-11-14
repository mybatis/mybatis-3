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
package org.apache.ibatis.executor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/** Verifies statement cleanup before ownership passes to a batch or cursor, using real HSQLDB statements. */
class ExecutorStatementLifecycleTest {

  private static final String SCHEMA = "org/apache/ibatis/submitted/sql/CreateDB-hsqldb.sql";

  /** Binding sentinels: {@link FailingIntTypeHandler} binds the real value and then raises. */
  private static final int SQL_FAILURE = -999_001;
  private static final int RUNTIME_FAILURE = -999_002;
  /** Binds normally, but HSQLDB cannot evaluate it as a divisor while executing. */
  private static final int DIVISION_BY_ZERO = 0;
  /** Binds below every identity value, so every fixture row matches. */
  private static final int ALL_ROWS = 0;
  private static final int ROW_COUNT = 3;

  private static final String TOUCH_USERS = UsersMapper.class.getName() + ".touchUsers";
  private static final String SELECT_IDS = UsersMapper.class.getName() + ".selectIds";
  private static final String SELECT_ORDERED_BY_DIVISOR = UsersMapper.class.getName() + ".selectOrderedByDivisor";

  private static UnpooledDataSource realDataSource;

  private ConnectionTracker tracker;
  private SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUpDatabase() throws Exception {
    realDataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:executor_statement_lifecycle",
        "sa", "");
    BaseDataTest.runScript(realDataSource, SCHEMA);
  }

  @BeforeEach
  void setUp() {
    tracker = new ConnectionTracker();
    Configuration configuration = new Configuration(
        new Environment("test", new JdbcTransactionFactory(), tracking(realDataSource, tracker)));
    // Registered for Integer, so every non sentinel value still binds through the real IntegerTypeHandler.
    configuration.getTypeHandlerRegistry().register(Integer.class, new FailingIntTypeHandler());
    configuration.addMapper(UsersMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  static Stream<Arguments> bindingFailures() {
    return Stream.of(arguments(ExecutorType.SIMPLE, Call.UPDATE, TOUCH_USERS, SQL_FAILURE, true),
        arguments(ExecutorType.SIMPLE, Call.QUERY, SELECT_IDS, RUNTIME_FAILURE, false),
        arguments(ExecutorType.SIMPLE, Call.CURSOR, SELECT_IDS, SQL_FAILURE, true),
        arguments(ExecutorType.BATCH, Call.UPDATE, TOUCH_USERS, SQL_FAILURE, true),
        arguments(ExecutorType.BATCH, Call.CURSOR, SELECT_IDS, SQL_FAILURE, true),
        arguments(ExecutorType.BATCH, Call.QUERY, SELECT_IDS, SQL_FAILURE, true));
  }

  @ParameterizedTest(name = "{0} {1} {4}")
  @MethodSource("bindingFailures")
  void shouldCloseStatementWhenBindingFails(ExecutorType executorType, Call call, String statementId, int parameter,
      boolean sqlFailure) {
    String scenario = executorType + " " + call;
    try (SqlSession session = sqlSessionFactory.openSession(executorType)) {
      PersistenceException failure = assertThrows(PersistenceException.class,
          () -> call(session, call, statementId, parameter));
      Class<? extends RuntimeException> expectedCause = sqlFailure ? TypeException.class : IllegalStateException.class;
      assertInstanceOf(expectedCause, failure.getCause(),
          scenario + ": a binding failure must not be wrapped further: " + failure.getCause());
      if (sqlFailure) {
        assertInstanceOf(SQLException.class, failure.getCause().getCause(),
            scenario + ": the driver's SQLException must be preserved as the cause");
      }
      if (call == Call.UPDATE && executorType == ExecutorType.BATCH) {
        // A half parameterized statement must never be published as a pending batch to flush later.
        assertTrue(session.flushStatements().isEmpty(), scenario + ": the failed statement must not become a batch");
      }
      assertClosedWithSessionOpen(tracker.onlyStatement(), scenario);
    }
  }

  static Stream<Arguments> executionFailures() {
    return Stream.of(arguments(ExecutorType.SIMPLE, Call.QUERY), arguments(ExecutorType.SIMPLE, Call.CURSOR),
        arguments(ExecutorType.BATCH, Call.CURSOR));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("executionFailures")
  void shouldCloseStatementWhenExecutionFails(ExecutorType executorType, Call call) {
    try (SqlSession session = sqlSessionFactory.openSession(executorType)) {
      PersistenceException failure = assertThrows(PersistenceException.class,
          () -> call(session, call, SELECT_ORDERED_BY_DIVISOR, DIVISION_BY_ZERO));
      assertInstanceOf(SQLException.class, failure.getCause(),
          "the driver's SQLException must be preserved: " + failure.getCause());
      assertClosedWithSessionOpen(tracker.onlyStatement(), executorType + " " + call);
    }
  }

  @ParameterizedTest
  @EnumSource(value = Call.class, names = { "UPDATE", "QUERY" })
  void shouldCloseStatementAfterSuccessfulCall(Call call) {
    try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      int affected = call == Call.UPDATE ? session.update(TOUCH_USERS, ALL_ROWS)
          : session.selectList(SELECT_IDS, ALL_ROWS).size();
      assertEquals(ROW_COUNT, affected);
      assertClosedWithSessionOpen(tracker.onlyStatement(), "successful " + call);
    }
  }

  @Test
  void shouldKeepReusedBatchStatementWhenBindingFails() {
    try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      session.update(TOUCH_USERS, ALL_ROWS);
      assertThrows(PersistenceException.class, () -> session.update(TOUCH_USERS, SQL_FAILURE));
      // The reused statement was published before the failed bind, so it is neither replaced nor dropped.
      assertEquals(1, tracker.statements.size(), "the reused statement must not be replaced by a new one");
      Tracked statement = tracker.onlyStatement();
      assertEquals(0, statement.closeCount, "a reused statement stays tracked until the batch is flushed");
      List<BatchResult> results = session.flushStatements();
      assertEquals(1, results.size());
      assertArrayEquals(new int[] { ROW_COUNT }, results.get(0).getUpdateCounts(),
          "the successful bind must still be flushed, without the half parameterized call");
      assertClosedWithSessionOpen(statement, "batch reuse flush");
    }
  }

  @Test
  void shouldCloseReuseExecutorStatementOnSessionCloseWhenBindingFails() {
    SqlSession session = sqlSessionFactory.openSession(ExecutorType.REUSE);
    try {
      assertThrows(PersistenceException.class, () -> session.update(TOUCH_USERS, SQL_FAILURE));
      // ReuseExecutor keeps the statement in its cache, so it outlives the failed call and closes with the session.
      assertEquals(0, tracker.onlyStatement().closeCount, "ReuseExecutor must keep the statement reusable");
      assertFalse(tracker.onlyConnection().isClosed(), "a failing statement must not close the connection");
    } finally {
      session.close();
    }
    assertEquals(1, tracker.onlyStatement().closeCount, "the statement must be closed exactly once with the session");
    assertTrue(tracker.onlyStatement().isClosed());
  }

  @Test
  void shouldHandOffUsableCursor() throws Exception {
    try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      Cursor<Integer> cursor = session.selectCursor(SELECT_IDS, ALL_ROWS);
      Tracked statement = tracker.onlyStatement();
      assertEquals(0, statement.closeCount, "the cursor statement must not be closed before the hand-off");
      assertFalse(statement.isClosed());
      assertTrue(cursor.iterator().hasNext());
      assertTrue(cursor.isOpen());
      assertFalse(statement.isClosed(), "the statement must stay open while the cursor can still be read");
      cursor.close();
      assertFalse(cursor.isOpen());
      assertFalse(tracker.onlyConnection().isClosed());
    }
  }

  @Test
  void shouldNotSuppressOriginalFailureWhenStatementCloseFails() {
    try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      // Make the next statement to be created really close and then throw.
      tracker.closeFailureTarget = tracker.statements.size();
      PersistenceException failure = assertThrows(PersistenceException.class,
          () -> session.update(TOUCH_USERS, SQL_FAILURE));
      assertInstanceOf(TypeException.class, failure.getCause());
      assertInstanceOf(SQLException.class, failure.getCause().getCause());
      assertTrue(failure.getCause().getCause().getMessage().contains("while binding parameter"));
      assertClosedWithSessionOpen(tracker.onlyStatement(), "close failure");
    }
  }

  /** Reads the state now: closing the session or its connection would close the statement as a side effect. */
  private void assertClosedWithSessionOpen(Tracked statement, String scenario) {
    assertTrue(statement.closeCount >= 1, scenario + ": the executor must close the statement");
    assertTrue(statement.isClosed(), scenario + ": the real JDBC statement must be closed");
    assertFalse(tracker.onlyConnection().isClosed(), scenario + ": a failing statement must not close the connection");
  }

  private static void call(SqlSession session, Call call, String statementId, int parameter) {
    if (call == Call.UPDATE) {
      session.update(statementId, parameter);
    } else if (call == Call.QUERY) {
      session.selectList(statementId, parameter);
    } else {
      session.selectCursor(statementId, parameter);
    }
  }

  public interface UsersMapper {

    @Update("UPDATE users SET name = name WHERE user_id >= #{value}")
    int touchUsers(Integer value);

    @Select("SELECT user_id FROM users WHERE user_id >= #{value}")
    List<Integer> selectIds(Integer value);

    /** Binds fine, but HSQLDB evaluates the divisor while executing, so the driver fails inside {@code execute()}. */
    @Select("SELECT user_id FROM users ORDER BY user_id / #{value}")
    List<Integer> selectOrderedByDivisor(Integer value);
  }

  public static class FailingIntTypeHandler extends IntegerTypeHandler {

    // Every value that is not a sentinel is bound by the real IntegerTypeHandler, so statements stay parameterized.
    @Override
    public void setParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
      if (parameter != null && parameter == SQL_FAILURE) {
        ps.setInt(i, 0);
        throw new SQLException("simulated SQLException while binding parameter " + i, "08006");
      }
      if (parameter != null && parameter == RUNTIME_FAILURE) {
        ps.setInt(i, 0);
        throw new IllegalStateException("simulated RuntimeException while binding parameter " + i);
      }
      super.setParameter(ps, i, parameter, jdbcType);
    }
  }

  /** The {@code SqlSession} call driven by a parameterized row. */
  enum Call {
    UPDATE, QUERY, CURSOR
  }

  /** Delegates every {@link DataSource} method to the real one, wrapping the connections it hands out. */
  private static DataSource tracking(DataSource delegate, ConnectionTracker tracker) {
    return (DataSource) Proxy.newProxyInstance(ExecutorStatementLifecycleTest.class.getClassLoader(),
        new Class<?>[] { DataSource.class }, (proxy, method, args) -> {
          Object result = invokeDelegate(delegate, method, args);
          return result instanceof Connection ? tracker.trackConnection((Connection) result) : result;
        });
  }

  private static final class ConnectionTracker {

    private final List<Tracked> connections = new ArrayList<>();
    private final List<Tracked> statements = new ArrayList<>();
    private int closeFailureTarget = -1;

    Connection trackConnection(Connection connection) {
      Tracked tracked = new Tracked(connection, this, -1, Connection.class);
      connections.add(tracked);
      return (Connection) tracked.proxy;
    }

    Object trackStatement(Statement statement) {
      Tracked tracked = new Tracked(statement, this, statements.size(), PreparedStatement.class);
      statements.add(tracked);
      return tracked.proxy;
    }

    Tracked onlyStatement() {
      assertEquals(1, statements.size(), "expected exactly one JDBC statement to be created");
      return statements.get(0);
    }

    Tracked onlyConnection() {
      assertEquals(1, connections.size(), "expected the session to use exactly one connection");
      return connections.get(0);
    }
  }

  /** Proxy over one real driver object: counts {@code close()} calls and reports the driver's {@code isClosed()}. */
  private static final class Tracked implements InvocationHandler {

    private final Object delegate;
    private final ConnectionTracker tracker;
    private final int index;
    private final Object proxy;
    private int closeCount;

    private Tracked(Object delegate, ConnectionTracker tracker, int index, Class<?> type) {
      this.delegate = delegate;
      this.tracker = tracker;
      this.index = index;
      this.proxy = Proxy.newProxyInstance(Tracked.class.getClassLoader(), new Class<?>[] { type }, this);
    }

    boolean isClosed() {
      try {
        return delegate instanceof Connection ? ((Connection) delegate).isClosed() : ((Statement) delegate).isClosed();
      } catch (SQLException e) {
        throw new IllegalStateException("Could not read the JDBC state", e);
      }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String name = method.getName();
      if ("close".equals(name)) {
        closeCount++;
        // Really close first, then raise: a failing cleanup must not leave a resource behind.
        Object ignored = invokeDelegate(delegate, method, args);
        if (index >= 0 && index == tracker.closeFailureTarget) {
          throw new SQLException("simulated SQLException while closing the statement", "08006");
        }
        return ignored;
      }
      if ("equals".equals(name)) {
        return proxy == args[0];
      }
      if ("hashCode".equals(name)) {
        return System.identityHashCode(proxy);
      }
      Object result = invokeDelegate(delegate, method, args);
      return result instanceof Statement ? tracker.trackStatement((Statement) result) : result;
    }
  }

  private static Object invokeDelegate(Object target, Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
