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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.ibatis.BaseDataTest;
import org.junit.Test;

public class ResourcesTest extends BaseDataTest {

  private static final ClassLoader CLASS_LOADER = ResourcesTest.class.getClassLoader();

  @Test
  public void shouldGetUrlForResource() throws Exception {
    URL url = Resources.getResourceURL(JPETSTORE_PROPERTIES);
    assertTrue(url.toString().endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetUrlAsProperties() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Properties props = Resources.getUrlAsProperties(url.toString());
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetResourceAsProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(props.getProperty("driver"));
  }

  @Test
  public void shouldGetUrlAsStream() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    InputStream in = Resources.getUrlAsStream(url.toString());
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetUrlAsReader() throws Exception {
    URL url = Resources.getResourceURL(CLASS_LOADER, JPETSTORE_PROPERTIES);
    Reader in = Resources.getUrlAsReader(url.toString());
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsStream() throws Exception {
    InputStream in = Resources.getResourceAsStream(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsReader() throws Exception {
    Reader in = Resources.getResourceAsReader(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(in);
    in.close();
  }

  @Test
  public void shouldGetResourceAsFile() throws Exception {
    File file = Resources.getResourceAsFile(JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsFileWithClassloader() throws Exception {
    File file = Resources.getResourceAsFile(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertTrue(file.getAbsolutePath().replace('\\', '/').endsWith("jpetstore/jpetstore-hsqldb.properties"));
  }

  @Test
  public void shouldGetResourceAsPropertiesWithOutClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  public void shouldGetResourceAsPropertiesWithClassloader() throws Exception {
    Properties file = Resources.getResourceAsProperties(CLASS_LOADER, JPETSTORE_PROPERTIES);
    assertNotNull(file);
  }

  @Test
  public void shouldAllowDefaultClassLoaderToBeSet() {
    Resources.setDefaultClassLoader(this.getClass().getClassLoader());
    assertEquals(this.getClass().getClassLoader(), Resources.getDefaultClassLoader());
  }

  @Test
  public void shouldAllowDefaultCharsetToBeSet() {
    Resources.setCharset(Charset.defaultCharset());
    assertEquals(Charset.defaultCharset(), Resources.getCharset());
  }

  @Test
  public void shouldGetClassForName() throws Exception {
    Class<?> clazz = Resources.classForName(ResourcesTest.class.getName());
    assertNotNull(clazz);
  }

  @Test(expected = ClassNotFoundException.class)
  public void shouldNotFindThisClass() throws ClassNotFoundException {
    Resources.classForName("some.random.class.that.does.not.Exist");
  }

  @Test
  public void shouldGetReader() throws IOException {

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
  public void shouldGetReaderWithClassLoader() throws IOException {

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
  public void stupidJustForCoverage() {
    assertNotNull(new Resources());
  }
}
