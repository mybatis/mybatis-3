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
package com.ibatis.dao.engine.transaction.jdbc;

import com.ibatis.common.jdbc.DbcpConfiguration;
import com.ibatis.common.jdbc.SimpleDataSource;
import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/*
 * DaoTransactionManager implementation for JDBC
 */
public class JdbcDaoTransactionManager implements DaoTransactionManager {

  private DataSource dataSource;

  public void configure(Properties properties) {
    if (properties.containsKey("DataSource")) {
      String type = (String) properties.get("DataSource");
      if ("SIMPLE".equals(type)) {
        configureSimpleDataSource(properties);
      } else if ("DBCP".equals(type)) {
        configureDbcp(properties);
      } else if ("JNDI".equals(type)) {
        configureJndi(properties);
      } else {
        throw new DaoException("DAO Transaction Manager properties must include a value for 'DataSource' of SIMPLE, DBCP or JNDI.");
      }
    } else {
      throw new DaoException("DAO Transaction Manager properties must include a value for 'DataSource' of SIMPLE, DBCP or JNDI.");
    }
  }

  public DaoTransaction startTransaction() {
    return new JdbcDaoTransaction(dataSource);
  }

  private void configureSimpleDataSource(Map properties) {
    dataSource = new SimpleDataSource(properties);
  }

  private void configureDbcp(Map properties) {
    DbcpConfiguration dbcp = new DbcpConfiguration(properties);
    dataSource = dbcp.getDataSource();
  }

  private void configureJndi(Map properties) {
    try {
      Hashtable contextProps = getContextProperties(properties);
      InitialContext initCtx = null;
      if (contextProps == null) {
        initCtx = new InitialContext();
      } else {
        initCtx = new InitialContext(contextProps);
      }
      dataSource = (DataSource) initCtx.lookup((String) properties.get("DBJndiContext"));
    } catch (NamingException e) {
      throw new DaoException("There was an error configuring the DataSource from JNDI.  Cause: " + e, e);
    }
  }

  public void commitTransaction(DaoTransaction trans) {
    ((JdbcDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((JdbcDaoTransaction) trans).rollback();
  }

  private static Hashtable getContextProperties(Map allProps) {
    final String PREFIX = "context.";
    Hashtable contextProperties = null;
    Iterator keys = allProps.keySet().iterator();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      String value = (String) allProps.get(key);
      if (key.startsWith(PREFIX)) {
        if (contextProperties == null) {
          contextProperties = new Properties();
        }
        contextProperties.put(key.substring(PREFIX.length()), value);
      }
    }
    return contextProperties;
  }
}
