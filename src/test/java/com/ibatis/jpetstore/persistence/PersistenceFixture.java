package com.ibatis.jpetstore.persistence;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.dao.client.DaoManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class PersistenceFixture {

  private static final String driver = "org.hsqldb.jdbcDriver";
  private static final String url = "jdbc:hsqldb:mem:testfixture";
  private static final String username = "sa";
  private static final String password = "";
  private static final DaoManager daoManager;

  static {
    try {
      // DAO Manager Configuration
      Properties props = new Properties();
      props.setProperty("driver", driver);
      props.setProperty("url", url);
      props.setProperty("username", username);
      props.setProperty("password", password);
      daoManager = DaoConfig.newDaoManager(props);

      // Test Database Initialization
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

    } catch (Exception e) {
      throw new RuntimeException("Description.  Cause: " + e, e);
    }
  }

  public static DaoManager getDaoManager() {
    return daoManager;
  }

}
