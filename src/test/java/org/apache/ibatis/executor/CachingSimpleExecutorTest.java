package org.apache.ibatis.executor;

import org.apache.ibatis.transaction.Transaction;
import org.junit.Test;

public class CachingSimpleExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  protected Executor createExecutor(Transaction transaction) {
    return new CachingExecutor(new SimpleExecutor(config,transaction));
  }

}
