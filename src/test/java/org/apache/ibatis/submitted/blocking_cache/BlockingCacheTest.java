/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.submitted.blocking_cache;

import java.io.Reader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// issue #524
class BlockingCacheTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/blocking_cache/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/blocking_cache/CreateDB.sql");
  }

  @Test
  void testBlockingCache() {
    ExecutorService defaultThreadPool = Executors.newFixedThreadPool(2);

    long init = System.currentTimeMillis();

    for (int i = 0; i < 2; i++) {
      defaultThreadPool.execute(this::accessDB);
    }

    defaultThreadPool.shutdown();

    while (!defaultThreadPool.isTerminated()) {
    }

    long totalTime = System.currentTimeMillis() - init;
    Assertions.assertTrue(totalTime > 1000);
  }

  private void accessDB() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper pm = sqlSession.getMapper(PersonMapper.class);
      pm.findAll();
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        Assertions.fail(e.getMessage());
      }
    }
  }

  @Test
  void ensureLockIsAcquiredBeforePut() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      mapper.delete(-1);
      mapper.findAll();
      sqlSession.commit();
    }
  }

  @Test
  void ensureLockIsReleasedOnRollback() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      mapper.delete(-1);
      mapper.findAll();
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      mapper.findAll();
    }
  }

  @Test
  void blockCacheDeathLockTest() throws InterruptedException {
    CountDownLatch step = new CountDownLatch(2);
    CountDownLatch waitForExecute = new CountDownLatch(2);

    new Thread(() -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
        mapper.findById(1);
        step.countDown();
        step.await();

        mapper.findById(2);
        sqlSession.commit();
      } catch (InterruptedException e) {
        //ignore
      }
      waitForExecute.countDown();
    }).start();

    new Thread(() -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
        mapper.findById(2);
        step.countDown();
        step.await();
        
        mapper.findById(1);
        sqlSession.commit();
      } catch (InterruptedException e) {
        //ignore
      }
      waitForExecute.countDown();
    }).start();

    boolean await = waitForExecute.await(30, TimeUnit.SECONDS);
    if (!await) {
      throw new RuntimeException("BlockingCache has a risk of death lock!");
    }
  }
}
