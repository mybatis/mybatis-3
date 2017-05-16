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

import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for {@link MergingBatchExecutor}.
 *
 * @author Kazuki Shimizu
 * @since 3.4.2
 */
public class MergingBatchExecutorTest extends BaseExecutorTest {

  @Override
  protected Executor createExecutor(Transaction transaction) {
    return new MergingBatchExecutor(config, transaction);
  }

  @Test
  public void testMerge() throws SQLException {
    Executor executor = createExecutor(new JdbcTransaction(ds, null, false));
    try {
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatement(config);
      MappedStatement insertStatement2 = ExecutorTestHelper.prepareInsertAuthorMappedStatementWithAutoKey(config);

      executor.update(insertStatement, new Author(1099, "someone1", "******", "someone1@apache.org", null, Section.NEWS));

      executor.update(insertStatement2, new Author(0, "someone3", "******", "someone1@apache.org", null, Section.NEWS));

      executor.update(insertStatement, new Author(1100, "someone2", "******", "someone2@apache.org", null, Section.NEWS));

      executor.update(insertStatement2, new Author(0, "someone4", "******", "someone1@apache.org", null, Section.NEWS));

      List<BatchResult> results = executor.flushStatements();
      assertEquals(2, results.size());
      assertEquals("INSERT INTO author (id,username,password,email,bio,favourite_section) values(?,?,?,?,?,?)", results.get(0).getSql());
      assertEquals(2, results.get(0).getUpdateCounts().length);
      assertEquals(1, results.get(0).getUpdateCounts()[0]);
      assertEquals(1, results.get(0).getUpdateCounts()[1]);
      assertEquals("INSERT INTO author (username,password,email,bio,favourite_section) values(?,?,?,?,?)", results.get(1).getSql());
      assertEquals(2, results.get(1).getUpdateCounts().length);
      assertEquals(1, results.get(1).getUpdateCounts()[0]);
      assertEquals(1, results.get(1).getUpdateCounts()[1]);
    } finally {
      executor.close(true);
    }
  }

}
