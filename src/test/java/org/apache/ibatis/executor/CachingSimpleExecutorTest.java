/**
 *    Copyright 2009-2015 the original author or authors.
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

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Executors;

public class CachingSimpleExecutorTest extends BaseExecutorTest {

  @Test
  public void dummy() {
  }

  @Override
  protected Executor createExecutor(Transaction transaction) {
    return new CachingExecutor(new SimpleExecutor(config,transaction));
  }

  private final java.util.concurrent.Executor threadExecutor = Executors.newFixedThreadPool(20);

  @Test
  public void testMultiThreadQuery() {
    final Executor mybatisExecutor = createExecutor(new JdbcTransaction(ds, null, false));

    for (int i = 0; i < 20; i++) {
      this.threadExecutor.execute(() -> {
        try {
          MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
          List<Object> l = mybatisExecutor.query(selectStatement, 1, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
          System.out.println(l);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
  }
}
