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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.reflection.MetaObject;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;
import org.apache.ibatis.reflection.SystemMetaObject;

/*
 * Wrapper class to simplify use of DBCP
 */
public class DbcpConfiguration {

  private static final String ADD_DRIVER_PROPS_PREFIX = "Driver.";
  private static final int ADD_DRIVER_PROPS_PREFIX_LENGTH = ADD_DRIVER_PROPS_PREFIX.length();
  private DataSource dataSource;

  /*
   * Constructor to supply a map of properties
   *
   * @param properties - the map of configuration properties
   */
  public DbcpConfiguration(Map properties) {
    try {

      dataSource = legacyDbcpConfiguration(properties);
      if (dataSource == null) {
        dataSource = newDbcpConfiguration(properties);
      }

    } catch (Exception e) {
      throw new RuntimeException("Error initializing DbcpDataSourceFactory.  Cause: " + e, e);
    }
  }

  /*
   * Getter for DataSource
   *
   * @return The DataSource
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  private BasicDataSource newDbcpConfiguration(Map map) {
    BasicDataSource basicDataSource = new BasicDataSource();
    Iterator props = map.keySet().iterator();
    MetaObject metaDataSource = SystemMetaObject.forObject(basicDataSource);
    while (props.hasNext()) {
      String propertyName = (String) props.next();
      if (propertyName.startsWith(ADD_DRIVER_PROPS_PREFIX)) {
        String value = (String) map.get(propertyName);
        basicDataSource.addConnectionProperty(propertyName.substring(ADD_DRIVER_PROPS_PREFIX_LENGTH), value);
      } else if (metaDataSource.hasSetter(propertyName)) {
        String value = (String) map.get(propertyName);
        Object convertedValue = convertValue(basicDataSource, propertyName, value);
        metaDataSource.setValue(propertyName, convertedValue);
      }
    }
    return basicDataSource;
  }

  private Object convertValue(Object object, String propertyName, String value) {
    Object convertedValue = value;
    MetaObject metaObject = SystemMetaObject.forObject(object);
    Class targetType = metaObject.getSetterType(propertyName);
    if (targetType == Integer.class || targetType == int.class) {
      convertedValue = Integer.valueOf(value);
    } else if (targetType == Long.class || targetType == long.class) {
      convertedValue = Long.valueOf(value);
    } else if (targetType == Boolean.class || targetType == boolean.class) {
      convertedValue = Boolean.valueOf(value);
    }
    return convertedValue;
  }

  private BasicDataSource legacyDbcpConfiguration(Map map) {
    BasicDataSource basicDataSource = null;
    if (map.containsKey("JDBC.Driver")) {
      basicDataSource = new BasicDataSource();
      String driver = (String) map.get("JDBC.Driver");
      String url = (String) map.get("JDBC.ConnectionURL");
      String username = (String) map.get("JDBC.Username");
      String password = (String) map.get("JDBC.Password");
      String validationQuery = (String) map.get("Pool.ValidationQuery");
      String maxActive = (String) map.get("Pool.MaximumActiveConnections");
      String maxIdle = (String) map.get("Pool.MaximumIdleConnections");
      String maxWait = (String) map.get("Pool.MaximumWait");

      basicDataSource.setUrl(url);
      basicDataSource.setDriverClassName(driver);
      basicDataSource.setUsername(username);
      basicDataSource.setPassword(password);

      if (notEmpty(validationQuery)) {
        basicDataSource.setValidationQuery(validationQuery);
      }

      if (notEmpty(maxActive)) {
        basicDataSource.setMaxActive(Integer.parseInt(maxActive));
      }

      if (notEmpty(maxIdle)) {
        basicDataSource.setMaxIdle(Integer.parseInt(maxIdle));
      }

      if (notEmpty(maxWait)) {
        basicDataSource.setMaxWait(Integer.parseInt(maxWait));
      }

      for (Object o : map.keySet()) {
        String propertyName = (String) o;
        if (propertyName.startsWith(ADD_DRIVER_PROPS_PREFIX)) {
          String value = (String) map.get(propertyName);
          basicDataSource.addConnectionProperty(propertyName.substring(ADD_DRIVER_PROPS_PREFIX_LENGTH), value);
        }
      }
    }
    return basicDataSource;
  }

  private boolean notEmpty(String s) {
    return s != null && s.length() > 0;
  }


}
