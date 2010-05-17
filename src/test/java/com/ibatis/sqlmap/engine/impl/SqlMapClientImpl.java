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
