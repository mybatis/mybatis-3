package org.apache.ibatis.session;

import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

public class SqlSessionManager implements SqlSessionFactory, SqlSession {

  private final SqlSessionFactory sqlSessionFactory;
  private final SqlSession sqlSessionProxy;

  private ThreadLocal<SqlSession> localSqlSession = new ThreadLocal<SqlSession>();

  public SqlSessionManager(Reader reader) {
    this(new SqlSessionFactoryBuilder().build(reader, null, null));
  }

  public SqlSessionManager(Reader reader, String environment) {
    this(new SqlSessionFactoryBuilder().build(reader, environment, null));
  }

  public SqlSessionManager(Reader reader, Properties properties) {
    this(new SqlSessionFactoryBuilder().build(reader, null, properties));
  }

  public SqlSessionManager(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
    this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
        SqlSessionFactory.class.getClassLoader(),
        new Class[]{SqlSession.class},
        new SqlSessionInterceptor());
  }

  public void startManagedSession() {
    this.localSqlSession.set(openSession());
  }

  public void startManagedSession(boolean autoCommit) {
    this.localSqlSession.set(openSession(autoCommit));
  }

  public void startManagedSession(Connection connection) {
    this.localSqlSession.set(openSession(connection));
  }

  public void startManagedSession(TransactionIsolationLevel level) {
    this.localSqlSession.set(openSession(level));
  }

  public void startManagedSession(ExecutorType execType) {
    this.localSqlSession.set(openSession(execType));
  }

  public void startManagedSession(ExecutorType execType, boolean autoCommit) {
    this.localSqlSession.set(openSession(execType, autoCommit));
  }

  public void startManagedSession(ExecutorType execType, TransactionIsolationLevel level) {
    this.localSqlSession.set(openSession(execType, level));
  }

  public void startManagedSession(ExecutorType execType, Connection connection) {
    this.localSqlSession.set(openSession(execType, connection));
  }

  public SqlSession openSession() {
    return sqlSessionFactory.openSession();
  }

  public SqlSession openSession(boolean autoCommit) {
    return sqlSessionFactory.openSession(autoCommit);
  }

  public SqlSession openSession(Connection connection) {
    return sqlSessionFactory.openSession(connection);
  }

  public SqlSession openSession(TransactionIsolationLevel level) {
    return sqlSessionFactory.openSession(level);
  }

  public SqlSession openSession(ExecutorType execType) {
    return sqlSessionFactory.openSession(execType);
  }

  public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
    return sqlSessionFactory.openSession(execType, autoCommit);
  }

  public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
    return sqlSessionFactory.openSession(execType, level);
  }

  public SqlSession openSession(ExecutorType execType, Connection connection) {
    return sqlSessionFactory.openSession(execType, connection);
  }

  public Configuration getConfiguration() {
    return sqlSessionFactory.getConfiguration();
  }

  public Object selectOne(String statement) {
    return sqlSessionProxy.selectOne(statement);
  }

  public Object selectOne(String statement, Object parameter) {
    return sqlSessionProxy.selectOne(statement, parameter);
  }

  public List selectList(String statement) {
    return sqlSessionProxy.selectList(statement);
  }

  public List selectList(String statement, Object parameter) {
    return sqlSessionProxy.selectList(statement, parameter);
  }

  public List selectList(String statement, Object parameter, RowBounds rowBounds) {
    return sqlSessionProxy.selectList(statement, parameter, rowBounds);
  }

  public void select(String statement, ResultHandler handler) {
    sqlSessionProxy.select(statement, handler);
  }

  public void select(String statement, Object parameter, ResultHandler handler) {
    sqlSessionProxy.select(statement, parameter, handler);
  }

  public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
    sqlSessionProxy.select(statement, parameter, rowBounds, handler);
  }

  public int insert(String statement) {
    return sqlSessionProxy.insert(statement);
  }

  public int insert(String statement, Object parameter) {
    return sqlSessionProxy.insert(statement, parameter);
  }

  public int update(String statement) {
    return sqlSessionProxy.update(statement);
  }

  public int update(String statement, Object parameter) {
    return sqlSessionProxy.update(statement, parameter);
  }

  public int delete(String statement) {
    return sqlSessionProxy.delete(statement);
  }

  public int delete(String statement, Object parameter) {
    return sqlSessionProxy.delete(statement, parameter);
  }

  public <T> T getMapper(Class<T> type) {
    return sqlSessionProxy.getMapper(type);
  }

  public Connection getConnection() {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot get connection.  No managed session is started.");
    return sqlSession.getConnection();
  }

  public void clearCache() {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot clear the cache.  No managed session is started.");
    sqlSession.clearCache();
  }

  public void commit() {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot commit.  No managed session is started.");
    sqlSession.commit();
  }

  public void commit(boolean force) {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot commit.  No managed session is started.");
    sqlSession.commit(force);
  }

  public void rollback() {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot rollback.  No managed session is started.");
    sqlSession.rollback();
  }

  public void rollback(boolean force) {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot rollback.  No managed session is started.");
    sqlSession.rollback(force);
  }

  public void close() {
    final SqlSession sqlSession = localSqlSession.get();
    if (sqlSession == null) throw new SqlSessionException("Error:  Cannot close.  No managed session is started.");
    try {
      sqlSession.close();
    } finally {
      localSqlSession.set(null);
    }
  }

  private class SqlSessionInterceptor implements InvocationHandler {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final SqlSession sqlSesson = SqlSessionManager.this.localSqlSession.get();
        if (sqlSesson != null) {
          return method.invoke(sqlSesson, args);
        } else {
          final SqlSession autoSqlSession = openSession();
          try {
            final Object result = method.invoke(autoSqlSession, args);
            autoSqlSession.commit();
            return result;
          } catch (Throwable t) {
            autoSqlSession.rollback();
            throw t;
          } finally {
            autoSqlSession.close();
          }
        }
      }
    }

}
