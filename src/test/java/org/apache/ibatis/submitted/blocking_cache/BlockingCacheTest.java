/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.blocking_cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// issue #524
class BlockingCacheTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/blocking_cache/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/blocking_cache/CreateDB.sql");
  }

  @Test
  void blockingCache() throws InterruptedException {
    ExecutorService defaultThreadPool = Executors.newFixedThreadPool(2);

    long init = System.currentTimeMillis();

    for (int i = 0; i < 2; i++) {
      defaultThreadPool.execute(this::accessDB);
    }

    defaultThreadPool.shutdown();
    if (!defaultThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
      defaultThreadPool.shutdownNow();
    }

    long totalTime = System.currentTimeMillis() - init;
    Assertions.assertThat(totalTime).isGreaterThanOrEqualTo(1000);
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

  /**
   * With {@code readWrite = true} and {@code blocking = true} a cached payload that can no longer be deserialized makes
   * the delegate read throw while the blocking key lock is already held. The lock is only released by a successful put
   * or by a rollback of a key that was reported as a miss, so the failing read must not keep it.
   */
  @Test
  void cacheReadFailureMustNotLeakTheBlockingLock() throws Exception {
    sqlSessionFactory.getConfiguration().addMapper(FragilePersonMapper.class);
    Cache cache = sqlSessionFactory.getConfiguration().getCache(FragilePersonMapper.class.getName());
    Assertions.assertThat(cache).isInstanceOf(BlockingCache.class);
    // the production default (0) waits forever: bound the wait so a leaked lock fails the test instead of hanging it
    ((BlockingCache) cache).setTimeout(100);

    // a normal query stores the serialized rows through the framework
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<FragilePerson> people = sqlSession.getMapper(FragilePersonMapper.class).findAll();
      Assertions.assertThat(people).extracting(FragilePerson::getFirstname).containsExactlyInAnyOrder("Jane", "John");
      sqlSession.commit();
    }

    FragilePerson.failOnDeserialize = true;
    try {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        FragilePersonMapper mapper = sqlSession.getMapper(FragilePersonMapper.class);
        // the payload is there, so the framework reads it and has to surface the deserialization failure
        Assertions.assertThatThrownBy(mapper::findAll).as("the deserialization failure must be surfaced")
            .isInstanceOf(PersistenceException.class).hasMessageContaining("Error deserializing object")
            .hasRootCauseInstanceOf(IOException.class);
        sqlSession.rollback();
      }
    } finally {
      FragilePerson.failOnDeserialize = false;
    }

    // a new session has to reach the statement again and return the real rows
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<FragilePerson> people = sqlSession.getMapper(FragilePersonMapper.class).findAll();
      Assertions.assertThat(people).extracting(FragilePerson::getFirstname).containsExactlyInAnyOrder("Jane", "John");
    }
  }

  @CacheNamespace(readWrite = true, blocking = true)
  public interface FragilePersonMapper {

    @Select("select id, firstname, lastname from person")
    List<FragilePerson> findAll();
  }

  public static class FragilePerson implements Serializable {

    private static final long serialVersionUID = 1L;

    private static boolean failOnDeserialize;

    private int id;
    private String firstname;
    private String lastname;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      if (failOnDeserialize) {
        throw new IOException("deliberate deserialization failure of a cached row");
      }
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getFirstname() {
      return firstname;
    }

    public void setFirstname(String firstname) {
      this.firstname = firstname;
    }

    public String getLastname() {
      return lastname;
    }

    public void setLastname(String lastname) {
      this.lastname = lastname;
    }
  }
}
