package com.ibatis.sqlmap.engine.transaction;

import java.sql.SQLException;

public interface TransactionScope {
  Object execute(Transaction transaction) throws SQLException;
}
