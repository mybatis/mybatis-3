package com.ibatis.dao.engine.transaction.sqlmap;

import com.ibatis.common.resources.Resources;
import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * @see com.ibatis.dao.engine.transaction.DaoTransactionManager
 */
public class SqlMapDaoTransactionManager implements DaoTransactionManager {

  private SqlMapClient client;

  /**
   * Required by interface
   *
   * @param properties required by interface
   * @see com.ibatis.dao.engine.transaction.DaoTransactionManager
   */
  public void configure(Properties properties) {
    try {
      Reader reader = null;
      if (properties.containsKey("SqlMapConfigURL")) {
        reader = Resources.getUrlAsReader((String) properties.get("SqlMapConfigURL"));
      } else if (properties.containsKey("SqlMapConfigResource")) {
        reader = Resources.getResourceAsReader((String) properties.get("SqlMapConfigResource"));
      } else {
        throw new DaoException("SQLMAP transaction manager requires either 'SqlMapConfigURL' or 'SqlMapConfigResource' to be specified as a property.");
      }
      client = SqlMapClientBuilder.buildSqlMapClient(reader, properties);
    } catch (IOException e) {
      throw new DaoException("Error configuring SQL Map.  Cause: " + e);
    }
  }

  /**
   * Required by interface
   *
   * @return A new Transaction
   * @see com.ibatis.dao.engine.transaction.DaoTransactionManager
   */
  public DaoTransaction startTransaction() {
    return new SqlMapDaoTransaction(client);
  }

  /**
   * Required by interface
   *
   * @param trans Required by interface
   * @see com.ibatis.dao.engine.transaction.DaoTransactionManager
   */
  public void commitTransaction(DaoTransaction trans) {
    ((SqlMapDaoTransaction) trans).commit();
  }

  /**
   * Required by interface
   *
   * @param trans Required by interface
   * @see com.ibatis.dao.engine.transaction.DaoTransactionManager
   */
  public void rollbackTransaction(DaoTransaction trans) {
    ((SqlMapDaoTransaction) trans).rollback();
  }

}
