package com.ibatis.sqlmap.engine.transaction.jta;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.transaction.BaseTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.session.Configuration;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;
import java.sql.SQLException;
import java.util.Properties;

public class JtaTransactionConfig extends BaseTransactionConfig {

  private UserTransaction userTransaction;

  public Transaction newTransaction(Configuration configuration, int transactionIsolation) throws SQLException, TransactionException {
    return new JtaTransaction(configuration, userTransaction, dataSource, transactionIsolation);
  }

  public void setProperties(Properties props) throws SQLException, TransactionException {
    String utxName = null;
    try {
      utxName = (String) props.get("UserTransaction");
      InitialContext initCtx = new InitialContext();
      userTransaction = (UserTransaction) initCtx.lookup(utxName);
    } catch (NamingException e) {
      throw new SqlMapException("Error initializing JtaTransactionConfig while looking up UserTransaction (" + utxName + ").  Cause: " + e);
    }
  }

}

