/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.executor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Batch Executor for merging an sql that created using same mapper method regardless of calling order.
 *
 * @author Kazuki Shimizu
 * @version 3.4.2
 */
public class MergingBatchExecutor extends BatchExecutor {

  private final Map<String, Statement> statementMap = new LinkedHashMap<String, Statement>();
  private final Map<String, BatchResult> batchResultMap = new LinkedHashMap<String, BatchResult>();

  public MergingBatchExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  @Override
  public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
    final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
    final String sql = handler.getBoundSql().getSql();
    final String statementKey = sql.hashCode() + "_" + ms.hashCode();
    final Statement stmt;
    if (statementMap.containsKey(statementKey)) {
      stmt = statementMap.get(statementKey);
      applyTransactionTimeout(stmt);
      handler.parameterize(stmt);
      batchResultMap.get(statementKey).addParameterObject(parameterObject);
    } else {
      stmt = handler.prepare(getConnection(ms.getStatementLog()), transaction.getTimeout());
      handler.parameterize(stmt);
      statementMap.put(statementKey, stmt);
      batchResultMap.put(statementKey, new BatchResult(ms, sql, parameterObject));
    }
    handler.batch(stmt);
    return BATCH_UPDATE_RETURN_VALUE;
  }

  @Override
  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    try {
      if (isRollback) {
        return Collections.emptyList();
      }
      List<BatchResult> results = new ArrayList<BatchResult>();
      int i = 0;
      for (String statementKey : statementMap.keySet()) {
        Statement stmt = statementMap.get(statementKey);
        applyTransactionTimeout(stmt);
        BatchResult batchResult = batchResultMap.get(statementKey);
        executeBatch(stmt, batchResult, i, results);
        results.add(batchResult);
        i++;
      }
      return results;
    } finally {
      closeStatements(statementMap.values());
      statementMap.clear();
      batchResultMap.clear();
    }
  }

}
