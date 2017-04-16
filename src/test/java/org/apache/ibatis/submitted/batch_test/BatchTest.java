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
package org.apache.ibatis.submitted.batch_test;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;

public class BatchTest
{

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/batch_test/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/batch_test/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void shouldGetAUserNoException() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      User user   = mapper.getUser(1);

      user.setId(2);
      user.setName("User2");
      mapper.insertUser(user);
      Assert.assertEquals("Dept1", mapper.getUser(2).getDept().getName());
    }
    catch (Exception e)
    {
      Assert.fail(e.getMessage());

    }

    finally {
      sqlSession.commit();
      sqlSession.close();
    }
  }

  @Test
  public void shouldReturnAffectedRowsIfDisableBatch(){
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      User user   = mapper.getUser(1);

      user.setId(3);
      user.setName("User3");
      int affectedRows = mapper.insertUser(user);
      Assert.assertEquals(BatchExecutor.BATCH_UPDATE_RETURN_VALUE, affectedRows);

      affectedRows = updateInBatchMode(mapper, 3, "User3_1");
      Assert.assertEquals(BatchExecutor.BATCH_UPDATE_RETURN_VALUE, affectedRows);

      affectedRows = updateInBatchModeButDisableBatch(mapper, 3, "User3_2");
      Assert.assertEquals(1, affectedRows);

      affectedRows = updateInBatchMode(mapper, 3, "User3_1");
      Assert.assertEquals(BatchExecutor.BATCH_UPDATE_RETURN_VALUE, affectedRows);

    }
    catch (Exception e)
    {
      Assert.fail(e.getMessage());

    }

    finally {
      sqlSession.commit();
      sqlSession.close();
    }
  }

  @Test
  public void shouldFlushStatementIfDisableBatch(){
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      User user   = mapper.getUser(1);

      user.setId(4);
      user.setName("User4");

      mapper.insertUser(user);
      updateInBatchMode(mapper, 4, "User4_1");
      updateInBatchMode(mapper, 4, "User4_2");
      Assert.assertEquals(2, sqlSession.flushStatements().size());

      updateInBatchMode(mapper, 4, "User4_1");
      updateInBatchMode(mapper, 4, "User4_2");

      updateInBatchModeButDisableBatch(mapper, 4, "User4_3");
      Assert.assertEquals(0, sqlSession.flushStatements().size());

      updateInBatchMode(mapper, 4, "User4_1");
      updateInBatchMode(mapper, 4, "User4_2");
      Assert.assertEquals(1, sqlSession.flushStatements().size());

    }
    catch (Exception e)
    {
      Assert.fail(e.getMessage());

    }

    finally {
      sqlSession.commit();
      sqlSession.close();
    }
  }

  private int updateInBatchMode(Mapper mapper, int id, String name){
    User updateUser = new User();

    updateUser.setId(id);
    updateUser.setName(name);

    return mapper.updateUser(updateUser);
  }

  private int updateInBatchModeButDisableBatch(Mapper mapper, int id, String name){
    User updateUser = new User();

    updateUser.setId(id);
    updateUser.setName(name);

    return mapper.updateUserIfDisableBatch(updateUser);
  }



}
