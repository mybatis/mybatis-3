/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.io;

import static org.junit.jupiter.api.Assertions.*;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

import org.apache.ibatis.annotations.CacheNamespace;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ResolverUtil}.
 *
 * @author Pi Chen
 * @since 3.5.2
 */

class ResolverUtilTest {
  private static ClassLoader currentContextClassLoader;

  @BeforeAll
  static void setUp() {
    currentContextClassLoader = Thread.currentThread().getContextClassLoader();
  }

  @Test
  void getClasses() {
    assertEquals(new ResolverUtil<>().getClasses().size(), 0);
  }

  @Test
  void getClassLoader() {
    assertEquals(new ResolverUtil<>().getClassLoader(), currentContextClassLoader);
  }

  @Test
  void setClassLoader() {
    ResolverUtil resolverUtil = new ResolverUtil();
    AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
      resolverUtil.setClassLoader(new ClassLoader() {
      });
      return null;
    });
    assertNotEquals(resolverUtil.getClassLoader(), currentContextClassLoader);
  }

  @Test
  void findImplementationsWithNullPackageName() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.findImplementations(VFS.class, null);
    assertEquals(resolverUtil.getClasses().size(), 0);
  }

  @Test
  void findImplementations() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.findImplementations(VFS.class, "org.apache.ibatis.io");
    Set<Class<? extends VFS>> classSets = resolverUtil.getClasses();
    //org.apache.ibatis.io.VFS
    //org.apache.ibatis.io.DefaultVFS
    //org.apache.ibatis.io.JBoss6VFS
    assertEquals(classSets.size(), 3); //fail if add a new VFS implementation in this package!!!
    classSets.forEach(c -> assertTrue(VFS.class.isAssignableFrom(c)));
  }

  @Test
  void findAnnotatedWithNullPackageName() {
    ResolverUtil<Object> resolverUtil = new ResolverUtil<>();
    resolverUtil.findAnnotated(CacheNamespace.class, null);
    assertEquals(resolverUtil.getClasses().size(), 0);
  }

  @Test
  void findAnnotated() {
    ResolverUtil<Object> resolverUtil = new ResolverUtil<>();
    resolverUtil.findAnnotated(CacheNamespace.class, this.getClass().getPackage().getName());
    Set<Class<?>> classSets = resolverUtil.getClasses();
    //org.apache.ibatis.io.ResolverUtilTest.TestMapper
    assertEquals(classSets.size(), 1);
    classSets.forEach(c -> assertNotNull(c.getAnnotation(CacheNamespace.class)));
  }

  @Test
  void find() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(VFS.class), "org.apache.ibatis.io");
    Set<Class<? extends VFS>> classSets = resolverUtil.getClasses();
    //org.apache.ibatis.io.VFS
    //org.apache.ibatis.io.DefaultVFS
    //org.apache.ibatis.io.JBoss6VFS
    assertEquals(classSets.size(), 3);
    classSets.forEach(c -> assertTrue(VFS.class.isAssignableFrom(c)));
  }

  @Test
  void getPackagePath() {
    ResolverUtil resolverUtil = new ResolverUtil();
    assertNull(resolverUtil.getPackagePath(null));
    assertEquals(resolverUtil.getPackagePath("org.apache.ibatis.io"), "org/apache/ibatis/io");
  }

  @Test
  void addIfMatching() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.addIfMatching(new ResolverUtil.IsA(VFS.class), "org/apache/ibatis/io/DefaultVFS.class");
    resolverUtil.addIfMatching(new ResolverUtil.IsA(VFS.class), "org/apache/ibatis/io/VFS.class");
    Set<Class<? extends VFS>> classSets = resolverUtil.getClasses();
    assertEquals(classSets.size(), 2);
    classSets.forEach(c -> assertTrue(VFS.class.isAssignableFrom(c)));
  }

  @Test
  void addIfNotMatching() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.addIfMatching(new ResolverUtil.IsA(VFS.class), "org/apache/ibatis/io/Xxx.class");
    assertEquals(resolverUtil.getClasses().size(), 0);
  }

  @Test
  void testToString() {
    ResolverUtil.IsA isa = new ResolverUtil.IsA(VFS.class);
    assertTrue(isa.toString().contains(VFS.class.getSimpleName()));

    ResolverUtil.AnnotatedWith annotatedWith = new ResolverUtil.AnnotatedWith(CacheNamespace.class);
    assertTrue(annotatedWith.toString().contains("@" + CacheNamespace.class.getSimpleName()));
  }


  @CacheNamespace(readWrite = false)
  private interface TestMapper {
    //test ResolverUtil.findAnnotated method
  }

}
