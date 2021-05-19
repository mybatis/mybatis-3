/*
 *    Copyright 2009-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.ibatis.BaseDataTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassLoaderWrapperTest extends BaseDataTest {

  private ClassLoaderWrapper wrapper;
  private ClassLoader loader;
  private final String RESOURCE_NOT_FOUND = "some_resource_that_does_not_exist.properties";
  private final String CLASS_NOT_FOUND = "some.random.class.that.does.not.Exist";
  private final String CLASS_FOUND = "java.lang.Object";

  @BeforeEach
  void beforeClassLoaderWrapperTest() {
    wrapper = new ClassLoaderWrapper();
    loader = getClass().getClassLoader();
  }

  @Test
  void classForName() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_FOUND));
  }

  @Test
  void classForNameNotFound() {
    Assertions.assertThrows(ClassNotFoundException.class, () -> assertNotNull(wrapper.classForName(CLASS_NOT_FOUND)));
  }

  @Test
  void classForNameWithClassLoader() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_FOUND, loader));
  }

  @Test
  void getResourceAsURL() {
    assertNotNull(wrapper.getResourceAsURL(JPETSTORE_PROPERTIES));
  }

  @Test
  void getResourceAsURLNotFound() {
    assertNull(wrapper.getResourceAsURL(RESOURCE_NOT_FOUND));
  }

  @Test
  void getResourceAsURLWithClassLoader() {
    assertNotNull(wrapper.getResourceAsURL(JPETSTORE_PROPERTIES, loader));
  }

  @Test
  void getResourceAsStream() {
    assertNotNull(wrapper.getResourceAsStream(JPETSTORE_PROPERTIES));
  }

  @Test
  void getResourceAsStreamNotFound() {
    assertNull(wrapper.getResourceAsStream(RESOURCE_NOT_FOUND));
  }

  @Test
  void getResourceAsStreamWithClassLoader() {
    assertNotNull(wrapper.getResourceAsStream(JPETSTORE_PROPERTIES, loader));
  }

}
