package org.apache.ibatis.session;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface SqlSession {

  Object selectOne(String statement);

  Object selectOne(String statement, Object parameter);

  List selectList(String statement);

  List selectList(String statement, Object parameter);

  List selectList(String statement, Object parameter, RowBounds rowBounds);

  Map selectMap(String statement, String mapKey);

  Map selectMap(String statement, Object parameter, String mapKey);

  Map selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);
  
  void select(String statement, Object parameter, ResultHandler handler);

  void select(String statement, ResultHandler handler);

  void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);

  int insert(String statement);

  int insert(String statement, Object parameter);

  int update(String statement);

  int update(String statement, Object parameter);

  int delete(String statement);

  int delete(String statement, Object parameter);

  void commit();

  void commit(boolean force);

  void rollback();

  void rollback(boolean force);

  void close();

  void clearCache();

  Configuration getConfiguration();

  <T> T getMapper(Class<T> type);

  Connection getConnection();
}
