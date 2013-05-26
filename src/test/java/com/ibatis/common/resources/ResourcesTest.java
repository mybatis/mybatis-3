/*
 *    Copyright 2009-2012 the original author or authors.
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
/*
 * Created on Apr 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ibatis.common.resources;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/*
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ResourcesTest extends TestCase {
  private boolean isUsingPrivateClassloader = false;

  public static void main(String[] args) {
    junit.textui.TestRunner.run(ResourcesTest.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testSetDefaultClassLoader() {
    ClassLoader classLoader = new TestCL(this.getClass().getClassLoader());
    ClassLoader tmp = Resources.getDefaultClassLoader();
    Resources.setDefaultClassLoader(classLoader);
    assertEquals(classLoader, Resources.getDefaultClassLoader());
    Resources.setDefaultClassLoader(tmp);
    assertNotSame(classLoader, Resources.getDefaultClassLoader());
  }

  /*
   * Class to test for URL getResourceURL(String)
   */
  public void testGetResourceURLString() {
    String resourceName;
    URL url;

    resourceName = "java/lang/String.class";
    url = null;
    try {
      url = Resources.getResourceURL(resourceName);
      assertNotNull(url);
      url = null;
    } catch (IOException e) {
      fail("Could not load resource " + resourceName);
    }

    resourceName = "java.lang.YouAreAMonkeyCoderIfYouHaveThisClass";
    url = null;
    try {
      Resources.getResourceURL(resourceName);
      fail("You are a monkey coder, and the Resources class loaded a bogus class.");
    } catch (IOException e) {
      //  This is expected...
      assertNull(url);
    }
  }

  /*
   * Class to test for URL getResourceURL(ClassLoader, String)
   */
  public void testGetResourceURLClassLoaderString() {
    String resourceName;
    URL url;
    ClassLoader classLoader;

    classLoader = new TestCL(this.getClass().getClassLoader());
    resourceName = "java/lang/String.class";
    url = null;
    isUsingPrivateClassloader = false;
    try {
      assertFalse(isUsingPrivateClassloader);
      url = Resources.getResourceURL(classLoader, resourceName);
      assertNotNull(url);
      assertTrue(isUsingPrivateClassloader);
      url = null;
    } catch (IOException e) {
      fail("Could not load resource " + resourceName);
    }

    resourceName = "java.lang.YouAreAMonkeyCoderIfYouHaveThisClass";
    url = null;
    isUsingPrivateClassloader = false;
    try {
      assertFalse(isUsingPrivateClassloader);
      Resources.getResourceURL(classLoader, resourceName);
      fail("You are a monkey coder, and the Resources class loaded a bogus class.");
    } catch (IOException e) {
      //  This is expected...
      assertNull(url);
      assertTrue(isUsingPrivateClassloader);
    }
  }

  /*
   * Class to test for InputStream getResourceAsStream(String)
   */
  public void testGetResourceAsStreamString() {
    InputStream inputStream;
    String resourceName;

    resourceName = "java/lang/String.class";
    inputStream = null;
    try {
      inputStream = Resources.getResourceAsStream(resourceName);
      assertNotNull(inputStream);
    } catch (IOException e) {
      fail("Could not load resource " + resourceName);
    }

    resourceName = "java.lang.YouAreAMonkeyCoderIfYouHaveThisClass";
    inputStream = null;
    try {
      Resources.getResourceURL(resourceName);
      fail("You are a monkey coder, and the Resources class loaded a bogus class.");
    } catch (IOException e) {
      //  This is expected...
      assertNull(inputStream);
    }
  }

  /*
   * Class to test for InputStream getResourceAsStream(ClassLoader, String)
   */
  public void testGetResourceAsStreamClassLoaderString() {
    InputStream inputStream;
    String resourceName;
    ClassLoader classLoader;

    classLoader = new TestCL(this.getClass().getClassLoader());
    resourceName = "java/lang/String.class";
    inputStream = null;
    isUsingPrivateClassloader = false;
    try {
      assertFalse(isUsingPrivateClassloader);
      inputStream = Resources.getResourceAsStream(classLoader, resourceName);
      assertNotNull(inputStream);
      assertTrue(isUsingPrivateClassloader);
    } catch (IOException e) {
      fail("Could not load resource " + resourceName);
    }

    resourceName = "java.lang.YouAreAMonkeyCoderIfYouHaveThisClass";
    inputStream = null;
    isUsingPrivateClassloader = false;
    try {
      assertFalse(isUsingPrivateClassloader);
      Resources.getResourceURL(classLoader, resourceName);
      fail("You are a monkey coder, and the Resources class loaded a bogus class.");
    } catch (IOException e) {
      //  This is expected...
      assertNull(inputStream);
      assertTrue(isUsingPrivateClassloader);
    }
  }

  /*
   * Class to test for Properties getResourceAsProperties(String)
   */
  public void testGetResourceAsPropertiesString() {
    String resourceName;
    String testProp = "name";
    String testPropValue = "value";
    String testProp2 = "name2";
    String testPropValue2 = "value2";
    Properties properties;

    resourceName = "com/ibatis/common/resources/resourcestest.properties";
    properties = null;
    try {
      properties = Resources.getResourceAsProperties(resourceName);
      assertNotNull(properties);
      assertEquals(properties.get(testProp), testPropValue);
      assertEquals(properties.get(testProp2), testPropValue2);
    } catch (IOException e) {
      fail("Could not read test properties file: " + resourceName);
    }

    resourceName = "com/ibatis/common/resources/dont-create-this-file-or-the-test-will-fail.properties";
    properties = null;

    try {
      properties = Resources.getResourceAsProperties(resourceName);
      fail("Are you TRYING to break this test? If not, Resources loaded a bad properties file: " + resourceName);
    } catch (IOException e) {
      assertNull(properties);
    }
  }

  /*
   * Class to test for Properties getResourceAsProperties(ClassLoader, String)
   */
  public void testGetResourceAsPropertiesClassLoaderString() {
    String resourceName;
    String testProp = "name";
    String testPropValue = "value";
    String testProp2 = "name2";
    String testPropValue2 = "value2";
    Properties properties;
    ClassLoader classLoader = new TestCL(this.getClass().getClassLoader());

    resourceName = "com/ibatis/common/resources/resourcestest.properties";
    properties = null;
    isUsingPrivateClassloader = false;
    try {
      assertNull(properties);
      assertFalse(isUsingPrivateClassloader);
      properties = Resources.getResourceAsProperties(classLoader, resourceName);
      assertNotNull(properties);
      assertTrue(isUsingPrivateClassloader);
      assertEquals(properties.get(testProp), testPropValue);
      assertEquals(properties.get(testProp2), testPropValue2);
    } catch (IOException e) {
      fail("Could not read test properties file: " + resourceName);
    }

    resourceName = "com/ibatis/common/resources/dont-create-this-file-or-the-test-will-fail.properties";
    properties = null;
    isUsingPrivateClassloader = false;
    try {
      assertFalse(isUsingPrivateClassloader);
      properties = Resources.getResourceAsProperties(classLoader, resourceName);
      fail("Are you TRYING to break this test? If not, Resources loaded a bad properties file: " + resourceName);
    } catch (IOException e) {
      assertNull(properties);
      assertTrue(isUsingPrivateClassloader);
    }
  }

  /*
   * Class to test for Reader getResourceAsReader(String)
   */
  public void testGetResourceAsReaderString() {
  }

  /*
   * Class to test for Reader getResourceAsReader(ClassLoader, String)
   */
  public void testGetResourceAsReaderClassLoaderString() {
  }

  /*
   * Class to test for File getResourceAsFile(String)
   */
  public void testGetResourceAsFileString() {
  }

  /*
   * Class to test for File getResourceAsFile(ClassLoader, String)
   */
  public void testGetResourceAsFileClassLoaderString() {
  }

  public void testGetUrlAsStream() {
  }

  public void testGetUrlAsReader() {
  }

  public void testGetUrlAsProperties() {
  }

  public void testClassForName() {
  }

  /*
   * Class to test for Object instantiate(String)
   */
  public void testInstantiateString() {
  }

  /*
   * Class to test for Object instantiate(Class)
   */
  public void testInstantiateClass() {
  }

  /* A stupid simple classloader for testing
   */
  private class TestCL extends ClassLoader {


    public TestCL(ClassLoader parent) {
      super(parent);
    }

    public synchronized void clearAssertionStatus() {
      isUsingPrivateClassloader = true;
      super.clearAssertionStatus();
    }

    protected Package definePackage(String name, String specTitle,
                                    String specVersion, String specVendor, String implTitle,
                                    String implVersion, String implVendor, URL sealBase)
        throws IllegalArgumentException {
      isUsingPrivateClassloader = true;
      return super.definePackage(name, specTitle, specVersion,
          specVendor, implTitle, implVersion, implVendor, sealBase);
    }

    protected Class findClass(String name) throws ClassNotFoundException {
      isUsingPrivateClassloader = true;
      return super.findClass(name);
    }

    protected String findLibrary(String libname) {
      isUsingPrivateClassloader = true;
      return super.findLibrary(libname);
    }

    protected URL findResource(String name) {
      isUsingPrivateClassloader = true;
      return super.findResource(name);
    }

    protected Enumeration findResources(String name) throws IOException {
      isUsingPrivateClassloader = true;
      return super.findResources(name);
    }

    protected Package getPackage(String name) {
      isUsingPrivateClassloader = true;
      return super.getPackage(name);
    }

    protected Package[] getPackages() {
      isUsingPrivateClassloader = true;
      return super.getPackages();
    }

    public URL getResource(String name) {
      isUsingPrivateClassloader = true;
      return super.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
      isUsingPrivateClassloader = true;
      return super.getResourceAsStream(name);
    }

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
      isUsingPrivateClassloader = true;
      return super.loadClass(name, resolve);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
      isUsingPrivateClassloader = true;
      return super.loadClass(name);
    }

    public synchronized void setClassAssertionStatus(String className,
                                                     boolean enabled) {
      isUsingPrivateClassloader = true;
      super.setClassAssertionStatus(className, enabled);
    }

    public synchronized void setDefaultAssertionStatus(boolean enabled) {
      isUsingPrivateClassloader = true;
      super.setDefaultAssertionStatus(enabled);
    }

    public synchronized void setPackageAssertionStatus(String packageName,
                                                       boolean enabled) {
      isUsingPrivateClassloader = true;
      super.setPackageAssertionStatus(packageName, enabled);
    }
  }
}
