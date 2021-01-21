package org.apache.ibatis.executor.flush;

import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;

import java.util.List;

/**
 * @author : Daniel Kvasnička
 * @inheritDoc
 * @since : 21.01.2021
 **/
public class DefaultFlushResultHandler implements FlushResultHandler {

  private final Executor executor;
  private final List<BatchResult> batchResults;

  public DefaultFlushResultHandler(Executor executor, List<BatchResult> batchResults) {
    this.executor = executor;
    this.batchResults = batchResults;
  }

  public List<BatchResult> handleResults() {
    return this.batchResults;
  }

  public Executor getExecutor() {
    return this.executor;
  }

}
