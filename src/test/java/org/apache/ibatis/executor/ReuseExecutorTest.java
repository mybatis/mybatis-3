package org.apache.ibatis.executor;

import org.apache.ibatis.transaction.Transaction;
import org.junit.Test;

public class ReuseExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  @Test
  public void shouldFetchPostWithBlogWithCompositeKey() throws Exception {
    super.shouldFetchPostWithBlogWithCompositeKey();
  }

  protected Executor createExecutor(Transaction transaction) {
    return new ReuseExecutor(config,transaction);
  }
}
