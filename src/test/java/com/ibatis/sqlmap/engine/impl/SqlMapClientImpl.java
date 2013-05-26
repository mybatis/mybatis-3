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
package com.ibatis.sqlmap.engine.impl;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.engine.builder.Ibatis2Configuration;
import org.apache.ibatis.cache.Cache;

import java.sql.Connection;

public class SqlMapClientImpl extends SqlMapSessionImpl implements SqlMapClient {

  public SqlMapClientImpl(Ibatis2Configuration configuration) {
    super(configuration);
  }

  public SqlMapSession openSession() {
    return new SqlMapSessionImpl(configuration);
  }

  public SqlMapSession openSession(Connection conn) {
    return new SqlMapSessionImpl(configuration, conn);
  }

  public void flushDataCache() {
    for (Cache c : configuration.getCaches()) {
      c.clear();
    }
  }

  public void flushDataCache(String cacheId) {
    configuration.getCache(cacheId).clear();
  }
}
