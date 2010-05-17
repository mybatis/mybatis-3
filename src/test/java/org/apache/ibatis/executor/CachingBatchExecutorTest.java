package org.apache.ibatis.executor;

import org.apache.ibatis.transaction.Transaction;
import org.junit.Test;

public class CachingBatchExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  protected Executor createExecutor(Transaction transaction) {
    return new CachingExecutor(new BatchExecutor(config,transaction));
  }

}