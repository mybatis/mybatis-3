/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.cache;

import java.io.Reader;
import java.lang.reflect.Field;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Property;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.annotations.CacheNamespaceRef;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

// issue #524
public class CacheTest {

  private static SqlSessionFactory sqlSessionFactory;

  @Before
  public void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cache/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/cache/CreateDB.sql");
  }

  /*
   * Test Plan: 
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "delete from A where id = 1"
   *  4) SqlSession 2 executes "select * from A"
   *
   * Assert:
   *   Step 4 returns 1 row. (This case fails when caching is enabled.)
   */
  @Test
  public void testplan1() {
    try (SqlSession sqlSession1 = sqlSessionFactory.openSession(false)) {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }

    try (SqlSession sqlSession2 = sqlSessionFactory.openSession(false)) {
      try {
        PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
        pm.delete(1);
        Assert.assertEquals(1, pm.findAll().size());
      } finally {
        sqlSession2.commit();
      }
    }
  }

  /*
   * Test Plan: 
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "delete from A where id = 1"
   *  4) SqlSession 2 executes "select * from A"
   *  5) SqlSession 2 rollback
   *  6) SqlSession 3 executes "select * from A"
   *
   * Assert:
   *   Step 6 returns 2 rows. 
   */
  @Test
  public void testplan2() {
    try (SqlSession sqlSession1 = sqlSessionFactory.openSession(false)) {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }

    try (SqlSession sqlSession2 = sqlSessionFactory.openSession(false)) {
      try {
        PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
        pm.delete(1);
      } finally {
        sqlSession2.rollback();
      }
    }

    try (SqlSession sqlSession3 = sqlSessionFactory.openSession(false)) {
      PersonMapper pm = sqlSession3.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }
  }

  /*
   * Test Plan with Autocommit on:
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "delete from A where id = 1"
   *  4) SqlSession 2 closes.
   *  5) SqlSession 2 executes "select * from A".
   *  6) SqlSession 3 closes.
   *
   * Assert:
   *   Step 6 returns 1 row. 
   */
  @Test
  public void testplan3() {
    try (SqlSession sqlSession1 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }


    try (SqlSession sqlSession2 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
      pm.delete(1);
    }

    try (SqlSession sqlSession3 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession3.getMapper(PersonMapper.class);
      Assert.assertEquals(1, pm.findAll().size());
    }
  }

  /*-
   * Test case for #405
   *
   * Test Plan with Autocommit on:
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "insert into person (id, firstname, lastname) values (3, hello, world)"
   *  4) SqlSession 2 closes.
   *  5) SqlSession 3 executes "select * from A".
   *  6) SqlSession 3 closes.
   *
   * Assert:
   *   Step 5 returns 3 row.
   */
  @Test
  public void shouldInsertWithOptionsFlushesCache() {
    try (SqlSession sqlSession1 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }

    try (SqlSession sqlSession2 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
      Person p = new Person(3, "hello", "world");
      pm.createWithOptions(p);
    }

    try (SqlSession sqlSession3 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession3.getMapper(PersonMapper.class);
      Assert.assertEquals(3, pm.findAll().size());
    }
  }

  /*-
   * Test Plan with Autocommit on:
   *  1) SqlSession 1 executes select to cache result
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes insert without flushing cache
   *  4) SqlSession 2 closes.
   *  5) SqlSession 3 executes select (flushCache = false)
   *  6) SqlSession 3 closes.
   *  7) SqlSession 4 executes select (flushCache = true)
   *  8) SqlSession 4 closes.
   *
   * Assert:
   *   Step 5 returns 2 row.
   *   Step 7 returns 3 row.
   */
  @Test
  public void shouldApplyFlushCacheOptions() {
    try (SqlSession sqlSession1 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }

    try (SqlSession sqlSession2 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
      Person p = new Person(3, "hello", "world");
      pm.createWithoutFlushCache(p);
    }

    try (SqlSession sqlSession3 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession3.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }

    try (SqlSession sqlSession4 = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession4.getMapper(PersonMapper.class);
      Assert.assertEquals(3, pm.findWithFlushCache().size());
    }
  }

  @Test
  public void shouldApplyCacheNamespaceRef() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
      Person p = new Person(3, "hello", "world");
      pm.createWithoutFlushCache(p);
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      ImportantPersonMapper pm = sqlSession.getMapper(ImportantPersonMapper.class);
      Assert.assertEquals(3, pm.findWithFlushCache().size());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession.getMapper(PersonMapper.class);
      Assert.assertEquals(3, pm.findAll().size());
      Person p = new Person(4, "foo", "bar");
      pm.createWithoutFlushCache(p);
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      SpecialPersonMapper pm = sqlSession.getMapper(SpecialPersonMapper.class);
      Assert.assertEquals(4, pm.findWithFlushCache().size());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
      PersonMapper pm = sqlSession.getMapper(PersonMapper.class);
      Assert.assertEquals(4, pm.findAll().size());
    }
  }

  @Test
  public void shouldApplyCustomCacheProperties() {
    CustomCache customCache = unwrap(sqlSessionFactory.getConfiguration().getCache(CustomCacheMapper.class.getName()));
    Assert.assertEquals("bar", customCache.getStringValue());
    Assert.assertEquals(1, customCache.getIntegerValue().intValue());
    Assert.assertEquals(2, customCache.getIntValue());
    Assert.assertEquals(3, customCache.getLongWrapperValue().longValue());
    Assert.assertEquals(4, customCache.getLongValue());
    Assert.assertEquals(5, customCache.getShortWrapperValue().shortValue());
    Assert.assertEquals(6, customCache.getShortValue());
    Assert.assertEquals((float) 7.1, customCache.getFloatWrapperValue(), 0);
    Assert.assertEquals((float)8.1, customCache.getFloatValue(), 0);
    Assert.assertEquals(9.01, customCache.getDoubleWrapperValue(), 0);
    Assert.assertEquals(10.01, customCache.getDoubleValue(), 0);
    Assert.assertEquals((byte)11, customCache.getByteWrapperValue().byteValue());
    Assert.assertEquals((byte)12, customCache.getByteValue());
    Assert.assertEquals(true, customCache.getBooleanWrapperValue());
    Assert.assertEquals(true, customCache.isBooleanValue());
  }

  @Test
  public void shouldErrorUnsupportedProperties() {
    when(sqlSessionFactory.getConfiguration()).addMapper(CustomCacheUnsupportedPropertyMapper.class);
    then(caughtException()).isInstanceOf(CacheException.class)
      .hasMessage("Unsupported property type for cache: 'date' of type class java.util.Date");
  }

  @Test
  public void shouldErrorInvalidCacheNamespaceRefAttributesSpecifyBoth() {
    when(sqlSessionFactory.getConfiguration().getMapperRegistry())
      .addMapper(InvalidCacheNamespaceRefBothMapper.class);
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessage("Cannot use both value() and name() attribute in the @CacheNamespaceRef");
  }

  @Test
  public void shouldErrorInvalidCacheNamespaceRefAttributesIsEmpty() {
    when(sqlSessionFactory.getConfiguration().getMapperRegistry())
      .addMapper(InvalidCacheNamespaceRefEmptyMapper.class);
    then(caughtException()).isInstanceOf(BuilderException.class)
      .hasMessage("Should be specified either value() or name() attribute in the @CacheNamespaceRef");
  }

  private CustomCache unwrap(Cache cache){
    Field field;
    try {
      field = cache.getClass().getDeclaredField("delegate");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
    try {
      field.setAccessible(true);
      return (CustomCache)field.get(cache);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } finally {
      field.setAccessible(false);
    }
  }

  @CacheNamespace(implementation = CustomCache.class, properties = {
      @Property(name = "date", value = "2016/11/21")
  })
  private interface CustomCacheUnsupportedPropertyMapper {
  }

  @CacheNamespaceRef(value = PersonMapper.class, name = "org.apache.ibatis.submitted.cache.PersonMapper")
  private interface InvalidCacheNamespaceRefBothMapper {
  }

  @CacheNamespaceRef
  private interface InvalidCacheNamespaceRefEmptyMapper {
  }

}
