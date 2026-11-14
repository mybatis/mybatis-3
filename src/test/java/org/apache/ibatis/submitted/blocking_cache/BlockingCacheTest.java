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

import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
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

  @Test
  void readOnlySessionCommitReleasesBlockingLocks() {
    registerFragileMapper();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      FragileMapper mapper = sqlSession.getMapper(FragileMapper.class);
      mapper.findAll();
      mapper.findById(1);
      sqlSession.commit();
    }
    Assertions.assertThat(getFragileCache().getSize()).as("both statements must use distinct cache keys").isEqualTo(2);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      FragileMapper mapper = sqlSession.getMapper(FragileMapper.class);
      Assertions.assertThat(mapper.findAll()).hasSize(2);
      Assertions.assertThat(mapper.findById(1)).hasSize(1);
    }
  }

  /**
   * A read-only session (no DML, so {@code dirty == false}) never rolls the transactional cache back; the cache is only
   * flushed on {@code commit()}. If that flush fails, every key whose value was not flushed yet must be released as
   * well, the original failure must be preserved and the session must be closable without releasing an already released
   * lock, otherwise all later sessions hang (or time out) on the leaked lock.
   */
  @Test
  void failedCommitOnReadOnlySessionMustNotLeakBlockingLocks() {
    registerFragileMapper();
    FragilePerson.failOnSerialize = true;
    AtomicReference<Throwable> commitFailure = new AtomicReference<>();
    AtomicReference<Throwable> closeFailure = new AtomicReference<>();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      FragileMapper mapper = sqlSession.getMapper(FragileMapper.class);
      // two cache misses on two different cache keys, both values fail to serialize on flush
      mapper.findAll();
      mapper.findById(1);
      try {
        sqlSession.commit();
      } catch (Throwable e) {
        commitFailure.set(e);
      }
    } catch (Throwable e) {
      closeFailure.set(e);
    } finally {
      // the recovery below must fail because of a leaked lock, not because of a failing serialization
      FragilePerson.failOnSerialize = false;
    }

    // recovery: a new session must be able to read both keys, whichever key was left behind
    AtomicReference<Throwable> findAllFailure = new AtomicReference<>();
    AtomicReference<Throwable> findByIdFailure = new AtomicReference<>();
    AtomicInteger findAllSize = new AtomicInteger(-1);
    AtomicInteger findByIdSize = new AtomicInteger(-1);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      FragileMapper mapper = sqlSession.getMapper(FragileMapper.class);
      try {
        findAllSize.set(mapper.findAll().size());
      } catch (Throwable e) {
        findAllFailure.set(e);
      }
      try {
        findByIdSize.set(mapper.findById(1).size());
      } catch (Throwable e) {
        findByIdFailure.set(e);
      }
    }

    assertAll(
        () -> Assertions.assertThat(commitFailure.get())
            .as("commit() must fail with the original serialization failure").isInstanceOf(PersistenceException.class)
            .hasMessageContaining("Error committing transaction").hasRootCauseInstanceOf(IOException.class),
        () -> Assertions.assertThat(closeFailure.get())
            .as("closing the session must not release an already released blocking cache lock").isNull(),
        () -> Assertions.assertThat(findAllFailure.get())
            .as("findAll() on a new session must not be blocked by a lock leaked on commit failure").isNull(),
        () -> Assertions.assertThat(findByIdFailure.get())
            .as("findById() on a new session must not be blocked by a lock leaked on commit failure").isNull(),
        () -> Assertions.assertThat(findAllSize.get()).as("findAll() on a new session must still return the rows")
            .isEqualTo(2),
        () -> Assertions.assertThat(findByIdSize.get()).as("findById() on a new session must still return the row")
            .isEqualTo(1));
  }

  /**
   * The failure must clean up every namespace of the transaction: {@code TransactionalCacheManager} commits the
   * namespaces one by one, so a failure in the first one must not abandon the locks the others still hold (a read-only
   * session never rolls them back, it only commits again on close). Both namespaces buffer a value that cannot be
   * serialized, so the iteration order of the transactional caches cannot decide the outcome.
   */
  @Test
  void failedCommitMustReleaseBlockingLocksOfAllNamespaces() {
    registerFragileMapper(FragileMapper.class);
    registerFragileMapper(OtherFragileMapper.class);
    FragilePerson.failOnSerialize = true;
    AtomicReference<Throwable> commitFailure = new AtomicReference<>();
    AtomicReference<Throwable> closeFailure = new AtomicReference<>();
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      // one buffered entry per namespace, both values fail to serialize on flush
      sqlSession.getMapper(FragileMapper.class).findAll();
      sqlSession.getMapper(OtherFragileMapper.class).findAll();
      try {
        sqlSession.commit();
      } catch (Throwable e) {
        commitFailure.set(e);
      }
    } catch (Throwable e) {
      closeFailure.set(e);
    } finally {
      FragilePerson.failOnSerialize = false;
    }

    // recovery: every namespace must be usable again, each attempt is observed even when one of them fails
    AtomicReference<Throwable> fragileFailure = new AtomicReference<>();
    AtomicReference<Throwable> otherFailure = new AtomicReference<>();
    AtomicInteger fragileSize = new AtomicInteger(-1);
    AtomicInteger otherSize = new AtomicInteger(-1);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        fragileSize.set(sqlSession.getMapper(FragileMapper.class).findAll().size());
      } catch (Throwable e) {
        fragileFailure.set(e);
      }
      try {
        otherSize.set(sqlSession.getMapper(OtherFragileMapper.class).findAll().size());
      } catch (Throwable e) {
        otherFailure.set(e);
      }
    }

    assertAll(
        () -> Assertions.assertThat(commitFailure.get())
            .as("commit() must fail with the original serialization failure").isInstanceOf(PersistenceException.class)
            .hasMessageContaining("Error committing transaction").hasRootCauseInstanceOf(IOException.class),
        () -> Assertions.assertThat(closeFailure.get())
            .as("closing the session must not release an already released blocking cache lock").isNull(),
        () -> Assertions.assertThat(fragileFailure.get())
            .as("the namespace that was committed first must not keep a lock on commit failure").isNull(),
        () -> Assertions.assertThat(otherFailure.get())
            .as("the namespaces that were not committed yet must not keep a lock on commit failure").isNull(),
        () -> Assertions.assertThat(fragileSize.get()).as("the first namespace must still return the rows")
            .isEqualTo(2),
        () -> Assertions.assertThat(otherSize.get()).as("the other namespace must still return the rows").isEqualTo(2));
  }

  private static void registerFragileMapper() {
    registerFragileMapper(FragileMapper.class);
  }

  private static void registerFragileMapper(Class<?> mapperType) {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    if (!configuration.hasMapper(mapperType)) {
      configuration.addMapper(mapperType);
    }
    // a bounded timeout turns a leaked lock into a failure instead of a hang
    ((BlockingCache) getFragileCache(mapperType)).setTimeout(100);
  }

  private static Cache getFragileCache() {
    return getFragileCache(FragileMapper.class);
  }

  private static Cache getFragileCache(Class<?> mapperType) {
    Cache cache = sqlSessionFactory.getConfiguration().getCache(mapperType.getName());
    Assertions.assertThat(cache).isInstanceOf(BlockingCache.class);
    return cache;
  }

  @CacheNamespace(readWrite = true, blocking = true)
  interface FragileMapper {

    @Select("select id, firstname, lastname from person")
    List<FragilePerson> findAll();

    @Select("select id, firstname, lastname from person where id = #{id}")
    List<FragilePerson> findById(int id);
  }

  @CacheNamespace(readWrite = true, blocking = true)
  interface OtherFragileMapper {

    @Select("select id, firstname, lastname from person")
    List<FragilePerson> findAll();
  }

  static class FragilePerson extends Person {

    private static final long serialVersionUID = 1L;

    static boolean failOnSerialize;

    private void writeObject(ObjectOutputStream out) throws IOException {
      if (failOnSerialize) {
        throw new IOException("simulated serialization failure");
      }
      out.defaultWriteObject();
    }
  }
}
