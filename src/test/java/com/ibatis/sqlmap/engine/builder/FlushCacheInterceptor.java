/*
 *    Copyright 2009-2012 the original author or authors.
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
