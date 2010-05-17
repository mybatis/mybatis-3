package com.ibatis.sqlmap.engine.builder;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.util.*;

@Intercepts({
  @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
  @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
    })
public class FlushCacheInterceptor implements Interceptor {

  private Map<String, Set<Cache>> flushCacheMap = new HashMap<String, Set<Cache>>();

  public Object intercept(Invocation invocation) throws Throwable {
    MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
    if (statement != null) {
      Set<Cache> cachesToFlush = flushCacheMap.get(statement.getId());
      if (cachesToFlush != null) {
        for (Cache c : cachesToFlush) {
          c.clear();
        }
      }
    }
    return invocation.proceed();
  }

  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  public void setProperties(Properties properties) {
  }

  public void addFlushOnExecute(String statementId, Cache cache) {
    Set<Cache> cachesToFlush = flushCacheMap.get(statementId);
    if (cachesToFlush == null) {
      cachesToFlush = new HashSet<Cache>();
      flushCacheMap.put(statementId, cachesToFlush);
    }
    cachesToFlush.add(cache);
  }

}
