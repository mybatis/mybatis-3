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

import org.apache.ibatis.executor.dialect.DerbyDialect;
import org.apache.ibatis.executor.dialect.Dialect;
import org.apache.ibatis.transaction.Transaction;
import org.junit.Test;

public class PagingCachingSimpleExecutorTest extends BaseExecutorTest {
  private Dialect dialect = new DerbyDialect();

  public PagingCachingSimpleExecutorTest() {
    super();
    config.setUsePagingExcutor(true);
    config.setDialect(dialect);
  }

  @Test
  public void dummy() {
  }

  @Override
  protected Executor createExecutor(Transaction transaction) {
    return new PagingExecutor(new CachingExecutor(new SimpleExecutor(config,transaction)),dialect);
  }

}
