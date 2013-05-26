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
package com.ibatis.sqlmap.engine.execution;

import org.apache.ibatis.executor.BatchExecutorException;

import java.sql.BatchUpdateException;
import java.util.List;

/*
 * This exception is thrown if a <code>java.sql.BatchUpdateException</code> is caught
 * during the execution of any nested batch.  The exception contains the
 * java.sql.BatchUpdateException that is the root cause, as well as
 * the results from any prior nested batch that executed successfully.  This exception
 * is only thrown from the executeBatchDetailed method.
 *
 * @author Jeff Butler
 */
public class BatchException extends RuntimeException {

  private List successfulBatchResults;
  private BatchUpdateException batchUpdateException;
  private String failingSqlStatement;
  private String failingStatementId;

  public BatchException(BatchExecutorException e) {
    this(e.getMessage(),
        e.getBatchUpdateException(),
        e.getSuccessfulBatchResults(),
        e.getFailingStatementId(),
        e.getFailingSqlStatement());
  }

  /*
   *
   */
  public BatchException(String message, BatchUpdateException cause, List successfulBatchResults,
                        String failingStatementId, String failingSqlStatement) {
    super(message, cause);
    this.batchUpdateException = cause;
    this.successfulBatchResults = successfulBatchResults;
    this.failingStatementId = failingStatementId;
    this.failingSqlStatement = failingSqlStatement;
  }

  /*
   * Returns the BatchUpdateException that caused the nested batch
   * to fail.  That exception contains an array of row counts
   * that can be used to determine exactly which statemtn of the
   * batch caused the failure (or failures).
   *
   * @return the root BatchUpdateException
   */
  public BatchUpdateException getBatchUpdateException() {
    return batchUpdateException;
  }

  /*
   * Returns a list of BatchResult objects.  There will be one entry
   * in the list for each successful sub-batch executed before the failing
   * batch.
   *
   * @return the previously successful batch results (may be an empty list
   *         if no batch has executed successfully)
   */
  public List getSuccessfulBatchResults() {
    return successfulBatchResults;
  }

  /*
   * Returns the SQL statement that caused the failure
   * (not the parameters)
   *
   * @return the failing SQL string
   */
  public String getFailingSqlStatement() {
    return failingSqlStatement;
  }

  /*
   * Returns the statement id of the statement that caused the failure
   *
   * @return the statement id
   */
  public String getFailingStatementId() {
    return failingStatementId;
  }
}
