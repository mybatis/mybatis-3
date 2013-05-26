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
package com.ibatis.common.jdbc;

import com.ibatis.common.resources.Resources;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class SimpleDataSource extends PooledDataSource {

  // Required Properties
  private static final String PROP_JDBC_DRIVER = "JDBC.Driver";
  private static final String PROP_JDBC_URL = "JDBC.ConnectionURL";
  private static final String PROP_JDBC_USERNAME = "JDBC.Username";
  private static final String PROP_JDBC_PASSWORD = "JDBC.Password";
  private static final String PROP_JDBC_DEFAULT_AUTOCOMMIT = "JDBC.DefaultAutoCommit";

  // Optional Properties
  private static final String PROP_POOL_MAX_ACTIVE_CONN = "Pool.MaximumActiveConnections";
  private static final String PROP_POOL_MAX_IDLE_CONN = "Pool.MaximumIdleConnections";
  private static final String PROP_POOL_MAX_CHECKOUT_TIME = "Pool.MaximumCheckoutTime";
  private static final String PROP_POOL_TIME_TO_WAIT = "Pool.TimeToWait";
  private static final String PROP_POOL_PING_QUERY = "Pool.PingQuery";
  private static final String PROP_POOL_PING_CONN_OLDER_THAN = "Pool.PingConnectionsOlderThan";
  private static final String PROP_POOL_PING_ENABLED = "Pool.PingEnabled";
  private static final String PROP_POOL_PING_CONN_NOT_USED_FOR = "Pool.PingConnectionsNotUsedFor";

  // Additional Driver Properties prefix
  private static final String ADD_DRIVER_PROPS_PREFIX = "Driver.";
  private static final int ADD_DRIVER_PROPS_PREFIX_LENGTH = ADD_DRIVER_PROPS_PREFIX.length();

  public SimpleDataSource(Map props) {
    try {
      if (props == null) {
        throw new RuntimeException("SimpleDataSource: The properties map passed to the initializer was null.");
      }

      if (props.containsKey(PROP_POOL_PING_CONN_OLDER_THAN)) {
        throw new UnsupportedOperationException("SimpleDataSource no longer supports " + PROP_POOL_PING_CONN_OLDER_THAN);
      }

      if (!(props.containsKey(PROP_JDBC_DRIVER)
          && props.containsKey(PROP_JDBC_URL)
          && props.containsKey(PROP_JDBC_USERNAME)
          && props.containsKey(PROP_JDBC_PASSWORD))) {
        throw new RuntimeException("SimpleDataSource: Some properties were not set.");
      }

      setDriver((String) props.get(PROP_JDBC_DRIVER));
      setUrl((String) props.get(PROP_JDBC_URL));
      setUsername((String) props.get(PROP_JDBC_USERNAME));
      setPassword((String) props.get(PROP_JDBC_PASSWORD));

      setPoolMaximumActiveConnections(
          props.containsKey(PROP_POOL_MAX_ACTIVE_CONN)
              ? Integer.parseInt((String) props.get(PROP_POOL_MAX_ACTIVE_CONN))
              : 10);

      setPoolMaximumIdleConnections(
          props.containsKey(PROP_POOL_MAX_IDLE_CONN)
              ? Integer.parseInt((String) props.get(PROP_POOL_MAX_IDLE_CONN))
              : 5);

      setPoolMaximumCheckoutTime(
          props.containsKey(PROP_POOL_MAX_CHECKOUT_TIME)
              ? Integer.parseInt((String) props.get(PROP_POOL_MAX_CHECKOUT_TIME))
              : 20000);

      setPoolTimeToWait(
          props.containsKey(PROP_POOL_TIME_TO_WAIT)
              ? Integer.parseInt((String) props.get(PROP_POOL_TIME_TO_WAIT))
              : 20000);

      setPoolPingEnabled(
          props.containsKey(PROP_POOL_PING_ENABLED)
              && Boolean.valueOf((String) props.get(PROP_POOL_PING_ENABLED)).booleanValue());

      setPoolPingQuery(
          props.containsKey(PROP_POOL_PING_QUERY)
              ? (String) props.get(PROP_POOL_PING_QUERY)
              : "NO PING QUERY SET");

      setPoolPingConnectionsNotUsedFor(
          props.containsKey(PROP_POOL_PING_CONN_NOT_USED_FOR)
              ? Integer.parseInt((String) props.get(PROP_POOL_PING_CONN_NOT_USED_FOR))
              : 0);

      setDefaultAutoCommit(
          props.containsKey(PROP_JDBC_DEFAULT_AUTOCOMMIT)
              && Boolean.valueOf((String) props.get(PROP_JDBC_DEFAULT_AUTOCOMMIT)).booleanValue());

      Properties driverProps = new Properties();
      driverProps.setProperty("user", getUsername());
      driverProps.setProperty("password", getPassword());
      for (Map.Entry entry : (Set<Map.Entry>) props.entrySet()) {
        String name = (String) entry.getKey();
        String value = (String) entry.getValue();
        if (name.startsWith(ADD_DRIVER_PROPS_PREFIX)) {
          driverProps.put(name.substring(ADD_DRIVER_PROPS_PREFIX_LENGTH), value);
        }
      }
      setDriverProperties(driverProps);

      Resources.classForName(getDriver()).newInstance();

      if (isPoolPingEnabled() && (!props.containsKey(PROP_POOL_PING_QUERY) ||
          getPoolPingQuery().trim().length() == 0)) {
        throw new RuntimeException("SimpleDataSource: property '" + PROP_POOL_PING_ENABLED + "' is true, but property '" +
            PROP_POOL_PING_QUERY + "' is not set correctly.");
      }

    } catch (Exception e) {
      throw new RuntimeException("SimpleDataSource: Error while loading properties. Cause: " + e, e);
    }
  }

}
