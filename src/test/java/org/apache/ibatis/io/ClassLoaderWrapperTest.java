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
package org.apache.ibatis.io;

import org.apache.ibatis.BaseDataTest;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class ClassLoaderWrapperTest extends BaseDataTest {

  ClassLoaderWrapper wrapper;
  ClassLoader loader;
  private final String RESOURCE_NOT_FOUND = "some_resource_that_does_not_exist.properties";
  private final String CLASS_NOT_FOUND = "some.random.class.that.does.not.Exist";
  private final String CLASS_FOUND = "java.lang.Object";


  @Before
  public void beforeClassLoaderWrapperTest() {
    wrapper = new ClassLoaderWrapper();
    loader = getClass().getClassLoader();
  }

  @Test
  public void classForName() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_FOUND));
  }

  @Test(expected = ClassNotFoundException.class)
  public void classForNameNotFound() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_NOT_FOUND));
  }

  @Test
  public void classForNameWithClassLoader() throws ClassNotFoundException {
    assertNotNull(wrapper.classForName(CLASS_FOUND, loader));
  }

  @Test
  public void getResourceAsURL() {
    assertNotNull(wrapper.getResourceAsURL(JPETSTORE_PROPERTIES));
  }

  @Test
  public void getResourceAsURLNotFound() {
    assertNull(wrapper.getResourceAsURL(RESOURCE_NOT_FOUND));
  }

  @Test
  public void getResourceAsURLWithClassLoader() {
    assertNotNull(wrapper.getResourceAsURL(JPETSTORE_PROPERTIES, loader));
  }

  @Test
  public void getResourceAsStream() {
    assertNotNull(wrapper.getResourceAsStream(JPETSTORE_PROPERTIES));
  }

  @Test
  public void getResourceAsStreamNotFound() {
    assertNull(wrapper.getResourceAsStream(RESOURCE_NOT_FOUND));
  }

  @Test
  public void getResourceAsStreamWithClassLoader() {
    assertNotNull(wrapper.getResourceAsStream(JPETSTORE_PROPERTIES, loader));
  }

}
