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
package org.apache.ibatis.executor;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

public class BatchExecutor extends BaseExecutor {

  public static final int BATCH_UPDATE_RETURN_VALUE = Integer.MIN_VALUE + 1002;

  private final List<Statement> statementList = new ArrayList<Statement>();
  private final List<BatchResult> batchResultList = new ArrayList<BatchResult>();
  private String currentSql;
  private MappedStatement currentStatement;

  public BatchExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
    final Configuration configuration = ms.getConfiguration();
    final StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, RowBounds.DEFAULT, null, null);
    final BoundSql boundSql = handler.getBoundSql();
    final String sql = boundSql.getSql();
    final Statement stmt;
    if (sql.equals(currentSql) && ms.equals(currentStatement)) {
      int last = statementList.size() - 1;
      stmt = statementList.get(last);
      BatchResult batchResult = batchResultList.get(last);
      batchResult.addParameterObject(parameterObject);
    } else {
      Connection connection = getConnection(ms.getStatementLog());
      stmt = handler.prepare(connection);
      currentSql = sql;
      currentStatement = ms;
      statementList.add(stmt);
      batchResultList.add(new BatchResult(ms, sql, parameterObject));
    }
    handler.parameterize(stmt);
    handler.batch(stmt);
    return BATCH_UPDATE_RETURN_VALUE;
  }

  public <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql)
      throws SQLException {
    Statement stmt = null;
    try {
      flushStatements();
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(this, ms, parameterObject, rowBounds, resultHandler, boundSql);
      Connection connection = getConnection(ms.getStatementLog());
      stmt = handler.prepare(connection);
      handler.parameterize(stmt);
      return handler.<E>query(stmt, resultHandler);
    } finally {
      if (ms.getFetchType() == FetchType.DEFAULT) {
        closeStatement(stmt);
      }
    }
  }

  public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
    try {
      List<BatchResult> results = new ArrayList<BatchResult>();
      if (isRollback) {
        return Collections.emptyList();
      } else {
        for (int i = 0, n = statementList.size(); i < n; i++) {
          Statement stmt = statementList.get(i);
          BatchResult batchResult = batchResultList.get(i);
          try {
            batchResult.setUpdateCounts(stmt.executeBatch());
            MappedStatement ms = batchResult.getMappedStatement();
            List<Object> parameterObjects = batchResult.getParameterObjects();
            KeyGenerator keyGenerator = ms.getKeyGenerator();
            if (keyGenerator instanceof Jdbc3KeyGenerator) {
              Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
              jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
            } else {
              for (Object parameter : parameterObjects) {
                keyGenerator.processAfter(this, ms, stmt, parameter);
              }
            }
          } catch (BatchUpdateException e) {
            StringBuffer message = new StringBuffer();
            message.append(batchResult.getMappedStatement().getId())
                .append(" (batch index #")
                .append(i + 1)
                .append(")")
                .append(" failed.");
            if (i > 0) {
              message.append(" ")
                  .append(i)
                  .append(" prior sub executor(s) completed successfully, but will be rolled back.");
            }
            throw new BatchExecutorException(message.toString(), e, results, batchResult);
          }
          results.add(batchResult);
        }
        return results;
      }
    } finally {
      for (Statement stmt : statementList) {
        closeStatement(stmt);
      }
      currentSql = null;
      statementList.clear();
      batchResultList.clear();
    }
  }

}
