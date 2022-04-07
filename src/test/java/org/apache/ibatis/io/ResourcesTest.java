/*
 *    Copyright 2009-2022 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.ibatis.BaseDataTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcesTest extends BaseDataTest {

  private static final ClassLoader CLASS_LOADER = ResourcesTest.class.getClassLoader();

  @Test
  void shouldGetUrlForResource() throws Exception {
    URL url = Resources.getResourceURL(JPETSTORE_PROPERTIES);
    assertTrue(url.toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  void shouldGetUrlAsProperties() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Properties props = Resources.getUrlAsProperties(url.toString());
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  void shouldGetResourceAsProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  void shouldGetUrlAsStream() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    try (InputStream in = Resources.getUrlAsStream(url.toString())) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetUrlAsReader() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    try (Reader in = Resources.getUrlAsReader(url.toString())) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetResourceAsStream() throws Exception {
    try (InputStream in = Resources.getResourceAsStream(CLASS_LOADER, JPETSTORE_PROPERTIES)) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetResourceAsReader() throws Exception {
    try(Reader in = Resources.getResourceAsReader(CLASS_LOADER, JPETSTORE_PROPERTIES)) {
      assertNotNull(in);
    }
  }

  @Test
  void shouldGetResourceAsFile() throws Exception {
    File file = Resources.getResourceAsFile(JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  void shouldGetResourceAsFileWithClassloader() throws Exception {
    File file = Resources.getResourceAsFile(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  void shouldGetResourceAsPropertiesWithOutClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  void shouldGetResourceAsPropertiesWithClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  void shouldAllowDefaultClassLoaderToBeSet() {
    Resources.setDefaultClassLoader(this.getClass().getClassLoader());
    assertEquals(this.getClass().getClassLoader(), Resources.getDefaultClassLoader());
  }

  @Test
  void shouldAllowDefaultCharsetToBeSet() {
    Resources.setCharset(Charset.defaultCharset());
    assertEquals(Charset.defaultCharset(), Resources.getCharset());
  }

  @Test
  void shouldGetClassForName() throws Exception {
    Class<?> clazz = Resources.classForName(ResourcesTest.class.getName());
    assertNotNull(clazz);
  }

  @Test
  void shouldNotFindThisClass() {
    Assertions.assertThrows(ClassNotFoundException.class,
        () -> Resources.classForName("some.random.class.that.does.not.Exist"));
  }

  @Test
  void shouldGetReader() throws IOException {

    // save the value
    Charset charset = Resources.getCharset();

    // charset
    Resources.setCharset(Charset.forName("US-ASCII"));
    assertNotNull(Resources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // no charset
    Resources.setCharset(null);
    assertNotNull(Resources.getResourceAsReader(JPETSTORE_PROPERTIES));

    // clean up
    Resources.setCharset(charset);

  }

  @Test
  void shouldGetReaderWithClassLoader() throws IOException {

    // save the value
    Charset charset = Resources.getCharset();

    // charset
    Resources.setCharset(Charset.forName("US-ASCII"));
    assertNotNull(Resources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // no charset
    Resources.setCharset(null);
    assertNotNull(Resources.getResourceAsReader(getClass().getClassLoader(), JPETSTORE_PROPERTIES));

    // clean up
    Resources.setCharset(charset);

  }

  @Test
  void stupidJustForCoverage() {
    assertNotNull(new Resources());
  }
}
