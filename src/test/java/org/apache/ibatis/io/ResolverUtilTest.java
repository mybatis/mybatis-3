/**
 * Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.io;

import org.apache.ibatis.annotations.CacheNamespace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * Unit tests for {@link ResolverUtil}.
 *
 * @author Pi Chen
 * @since 3.5.2
 */

public class ResolverUtilTest {
  private ClassLoader currentContextClassLoader;

  @Before
  public void setUp() throws Exception {
    currentContextClassLoader = Thread.currentThread().getContextClassLoader();
  }

  @Test
  public void getClasses() {
    Assert.assertEquals(new ResolverUtil<>().getClasses().size(), 0);
  }

  @Test
  public void getClassLoader() {
    Assert.assertEquals(new ResolverUtil<>().getClassLoader(), currentContextClassLoader);
  }

  @Test
  public void setClassLoader() {
    ResolverUtil resolverUtil = new ResolverUtil();
    resolverUtil.setClassLoader(new ClassLoader() {
      // do nothing...
    });
    Assert.assertNotEquals(resolverUtil.getClassLoader(), currentContextClassLoader);
  }

  @Test
  public void findImplementationsWithNullPackageName() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.findImplementations(VFS.class, null);
    Assert.assertEquals(resolverUtil.getClasses().size(), 0);
  }

  @Test
  public void findImplementations() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.findImplementations(VFS.class, "org.apache.ibatis.io");
    Set<Class<? extends VFS>> classSets = resolverUtil.getClasses();
    classSets.forEach(c -> Assert.assertTrue(VFS.class.isAssignableFrom(c)));
  }

  @Test
  public void findAnnotatedWithNullPackageName() {
    ResolverUtil<Object> resolverUtil = new ResolverUtil<>();
    resolverUtil.findAnnotated(CacheNamespace.class, null);
    Assert.assertEquals(resolverUtil.getClasses().size(), 0);
  }

  @Test
  public void findAnnotated() {
    ResolverUtil<Object> resolverUtil = new ResolverUtil<>();
    resolverUtil.findAnnotated(CacheNamespace.class, "org.apache.ibatis.binding");
    Set<Class<?>> classSets = resolverUtil.getClasses();
    classSets.forEach(c -> Assert.assertNotNull(c.getAnnotation(CacheNamespace.class)));
  }

  @Test
  public void find() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(VFS.class), "org.apache.ibatis.io");
    Set<Class<? extends VFS>> classSets = resolverUtil.getClasses();
    classSets.forEach(c -> Assert.assertTrue(VFS.class.isAssignableFrom(c)));
  }

  @Test
  public void getPackagePath() {
    ResolverUtil resolverUtil = new ResolverUtil();
    Assert.assertNull(resolverUtil.getPackagePath(null));
    Assert.assertEquals(resolverUtil.getPackagePath("org.apache.ibatis.io"), "org/apache/ibatis/io");
  }

  @Test
  public void addIfMatching() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.addIfMatching(new ResolverUtil.IsA(VFS.class), "org/apache/ibatis/io/DefaultVFS.class");
    resolverUtil.addIfMatching(new ResolverUtil.IsA(VFS.class), "org/apache/ibatis/io/VFS.class");
    Set<Class<? extends VFS>> classSets = resolverUtil.getClasses();
    classSets.forEach(c -> Assert.assertTrue(VFS.class.isAssignableFrom(c)));
  }

  @Test
  public void addIfNotMatching() {
    ResolverUtil<VFS> resolverUtil = new ResolverUtil<>();
    resolverUtil.addIfMatching(new ResolverUtil.IsA(VFS.class), "org/apache/ibatis/io/Xxx.class");
    Assert.assertEquals(resolverUtil.getClasses().size(), 0);
  }

  @Test
  public void testToString() {
    ResolverUtil.IsA isa = new ResolverUtil.IsA(VFS.class);
    Assert.assertTrue(isa.toString().contains(VFS.class.getSimpleName()));

    ResolverUtil.AnnotatedWith annotatedWith = new ResolverUtil.AnnotatedWith(CacheNamespace.class);
    Assert.assertTrue(annotatedWith.toString().contains("@" + CacheNamespace.class.getSimpleName()));
  }

}
