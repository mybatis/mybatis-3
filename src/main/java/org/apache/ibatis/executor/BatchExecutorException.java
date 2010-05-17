package org.apache.ibatis.executor;

import java.sql.BatchUpdateException;
import java.util.List;

public class BatchExecutorException extends ExecutorException {

  private final List successfulBatchResults;
  private final BatchUpdateException batchUpdateException;
  private final BatchResult batchResult;

  public BatchExecutorException(String message, BatchUpdateException cause, List successfulBatchResults,
                                BatchResult batchResult) {
    super(message + " Cause: " + cause, cause);
    this.batchUpdateException = cause;
    this.successfulBatchResults = successfulBatchResults;
    this.batchResult = batchResult;
  }

  /**
   * Returns the BatchUpdateException that caused the nested executor
   * to fail.  That exception contains an array of row counts
   * that can be used to determine exactly which statemtn of the
   * executor caused the failure (or failures).
   *
   * @return the root BatchUpdateException
   */
  public BatchUpdateException getBatchUpdateException() {
    return batchUpdateException;
  }

  /**
   * Returns a list of BatchResult objects.  There will be one entry
   * in the list for each successful sub-executor executed before the failing
   * executor.
   *
   * @return the previously successful executor results (may be an empty list
   *         if no executor has executed successfully)
   */
  public List getSuccessfulBatchResults() {
    return successfulBatchResults;
  }

  /**
   * Returns the SQL statement that caused the failure
   * (not the parameterArray)
   *
   * @return the failing SQL string
   */
  public String getFailingSqlStatement() {
    return batchResult.getSql();
  }

  /**
   * Returns the statement id of the statement that caused the failure
   *
   * @return the statement id
   */
  public String getFailingStatementId() {
    return batchResult.getMappedStatement().getId();
  }
}
