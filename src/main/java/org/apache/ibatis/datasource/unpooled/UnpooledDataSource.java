package org.apache.ibatis.datasource.unpooled;

import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.io.Resources;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

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

  private synchronized void initializeDriver() {
    if (!driverInitialized) {
      driverInitialized = true;
      Class driverType;
      try {
        if (driverClassLoader != null) {
          driverType = Class.forName(driver, true, driverClassLoader);
        } else {
          driverType = Resources.classForName(driver);
        }
        DriverManager.registerDriver(new DriverProxy((Driver) driverType.newInstance()));
      } catch (Exception e) {
        throw new DataSourceException("Error setting driver on UnpooledDataSource. Cause: " + e, e);
      }
    }
  }

  private static class DriverProxy implements Driver {
    private Driver driver;

    DriverProxy(Driver d) {
      this.driver = d;
    }

    public boolean acceptsURL(String u) throws SQLException {
      return this.driver.acceptsURL(u);
    }

    public Connection connect(String u, Properties p) throws SQLException {
      return this.driver.connect(u, p);
    }

    public int getMajorVersion() {
      return this.driver.getMajorVersion();
    }

    public int getMinorVersion() {
      return this.driver.getMinorVersion();
    }

    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
      return this.driver.getPropertyInfo(u, p);
    }

    public boolean jdbcCompliant() {
      return this.driver.jdbcCompliant();
    }
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }
}
