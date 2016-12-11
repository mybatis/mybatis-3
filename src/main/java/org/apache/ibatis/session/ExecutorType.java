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
package org.apache.ibatis.session;

import org.apache.ibatis.executor.MergingBatchExecutor;

/**
 * Executor type.
 *
 * @see org.apache.ibatis.executor.Executor
 * @author Clinton Begin
 * @author Kazuki Shimizu
 *
 */
public enum ExecutorType {
  /**
   * Enable the simple executor.
   * @see org.apache.ibatis.executor.SimpleExecutor
   *
   */
  SIMPLE,
  /**
   * Enable the executor for reusing a prepared statement.
   * @see org.apache.ibatis.executor.ReuseExecutor
   */
  REUSE,
  /**
   * Enable the batch executor. (valid only when consecutively calling the same mapper method)
   * @see org.apache.ibatis.executor.BatchExecutor
   */
  BATCH,
  /**
   * Enable the batch executor for merging an SQL that created using same mapper method regardless of calling order.
   * @see MergingBatchExecutor
   * @since 3.4.2
   */
  MERGING_BATCH
}
