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
package org.apache.ibatis.session;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ReuseExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Clinton Begin
 */
public enum ExecutorType {
  SIMPLE(SimpleExecutor::new), REUSE(ReuseExecutor::new), BATCH(BatchExecutor::new);

  private ExecutorCreator creator;

  ExecutorType(ExecutorCreator creator){
      this.creator = creator;
  }

  public Executor createExecutor(Configuration configuration, Transaction transaction){
      return creator.create(configuration, transaction);
  }

  private interface ExecutorCreator{
    Executor create(Configuration configuration, Transaction transaction);
  }
}
