/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.datasource.unpooled;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.LogFactory;

public class UnpooledDataSource implements DataSource {

  private ClassLoader driverClassLoader;
  private Properties driverProperties;
  private boolean driverInitialized;

  private String driver;
  private String url;
  private String username;
  private String password;

  private boolean autoCommit;
  private Integer defaultTransactionIsolationLevel;

  static {
    DriverManager.getDrivers(); // see DBCP - 272
  }

  public UnpooledDataSource() {
  }

  public UnpooledDataSource(String driver, String url, String username, String password) {
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  public UnpooledDataSource(String driver, String url, Properties driverProperties) {
    this.driver = driver;
    this.url = url;
    this.driverProperties = driverProperties;
  }

  public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
    this.driverClassLoader = driverClassLoader;
    this.driver = driver;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
    this.driverClassLoader = driverClassLoader;
    this.driver = driver;
    this.url = url;
    this.driverProperties = driverProperties;
  }

  public Connection getConnection() throws SQLException {
    initializeDriver();
    Connection connection;
    if (driverProperties != null) {
      connection = DriverManager.getConnection(url, driverProperties);
    } else if (username == null && password == null) {
      connection = DriverManager.getConnection(url);
    } else {
      connection = DriverManager.getConnection(url, username, password);
    }
    configureConnection(connection);
    return connection;
  }

  public Connection getConnection(String username, String password) throws SQLException {
    initializeDriver();
    Connection connection = DriverManager.getConnection(url, username, password);
    configureConnection(connection);
    return connection;
  }

  public void setLoginTimeout(int loginTimeout) throws SQLException {
    DriverManager.setLoginTimeout(loginTimeout);
  }

  public int getLoginTimeout() throws SQLException {
    return DriverManager.getLoginTimeout();
  }

  public void setLogWriter(PrintWriter logWriter) throws SQLException {
    DriverManager.setLogWriter(logWriter);
  }

  public PrintWriter getLogWriter() throws SQLException {
    return DriverManager.getLogWriter();
  }

  public ClassLoader getDriverClassLoader() {
    return driverClassLoader;
  }

  public void setDriverClassLoader(ClassLoader driverClassLoader) {
    this.driverClassLoader = driverClassLoader;
  }

  public Properties getDriverProperties() {
    return driverProperties;
  }

  public void setDriverProperties(Properties driverProperties) {
    this.driverProperties = driverProperties;
  }

  public String getDriver() {
    return driver;
  }

  public synchronized void setDriver(String driver) {
    this.driver = driver;
    driverInitialized = false;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public Integer getDefaultTransactionIsolationLevel() {
    return defaultTransactionIsolationLevel;
  }

  public void setDefaultTransactionIsolationLevel(Integer defaultTransactionIsolationLevel) {
    this.defaultTransactionIsolationLevel = defaultTransactionIsolationLevel;
  }

  private void configureConnection(Connection conn) throws SQLException {
    if (autoCommit != conn.getAutoCommit()) {
      conn.setAutoCommit(autoCommit);
    }
    if (defaultTransactionIsolationLevel != null) {
      conn.setTransactionIsolation(defaultTransactionIsolationLevel);
    }
  }

  private synchronized void initializeDriver() throws SQLException {
    if (!driverInitialized) {
      driverInitialized = true;
      try {
        if (driverClassLoader != null) {
          Class.forName(driver, true, driverClassLoader);
        } else {
          Resources.classForName(driver);
        }
      } catch (Exception e) {
        throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e, e);
      }
    }
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException(getClass().getName() + " is not a wrapper.");
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  public Logger getParentLogger() {
    return Logger.getLogger(LogFactory.GLOBAL_LOGGER_NAME);
  }

}
