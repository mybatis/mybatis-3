package com.ibatis.jpetstore.persistence;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.client.DaoManagerBuilder;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DaoConfig {

  private static final String resource = "com/ibatis/jpetstore/persistence/dao.xml";
  private static final DaoManager daoManager;

  static {
    try {
      daoManager = newDaoManager(null);
      Properties props = Resources.getResourceAsProperties("com/properties/database.properties");
      String url = props.getProperty("url");
      String driver = props.getProperty("driver");
      String username = props.getProperty("username");
      String password = props.getProperty("password");
      if (url.equals("jdbc:hsqldb:mem:jpetstore")) {
        Class.forName(driver).newInstance();
        Connection conn = DriverManager.getConnection(url, username, password);
        try {
          ScriptRunner runner = new ScriptRunner(conn, false, false);
          runner.setErrorLogWriter(null);
          runner.setLogWriter(null);
          runner.runScript(Resources.getResourceAsReader("com/ddl/hsql/jpetstore-hsqldb-schema.sql"));
          runner.runScript(Resources.getResourceAsReader("com/ddl/hsql/jpetstore-hsqldb-dataload.sql"));
        } finally {
          conn.close();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Description.  Cause: " + e, e);
    }

  }

  public static DaoManager getDaoManager() {
    return daoManager;
  }

  public static DaoManager newDaoManager(Properties props) {
    try {
      Reader reader = Resources.getResourceAsReader(resource);
      return DaoManagerBuilder.buildDaoManager(reader, props);
    } catch (Exception e) {
      throw new RuntimeException("Could not initialize DaoConfig.  Cause: " + e, e);
    }
  }

}
