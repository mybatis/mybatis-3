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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * A class to simplify access to resources through the classloader.
 *
 * @author Clinton Begin
 */
public class Resources {

  private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

  /**
   * Charset to use when calling getResourceAsReader.
   * null means use the system default.
   */
  private static Charset charset;

  Resources() {
  }

  /**
   * Returns the default classloader (may be null).
   *
   * @return The default classloader
   */
  public static ClassLoader getDefaultClassLoader() {
    return classLoaderWrapper.defaultClassLoader;
  }

  /**
   * Sets the default classloader
   *
   * @param defaultClassLoader - the new default ClassLoader
   */
  public static void setDefaultClassLoader(ClassLoader defaultClassLoader) {
    classLoaderWrapper.defaultClassLoader = defaultClassLoader;
  }

  /**
   * Returns the URL of the resource on the classpath
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static URL getResourceURL(String resource) throws IOException {
    // issue #625
    return getResourceURL(null, resource);
  }

  /**
   * Returns the URL of the resource on the classpath
   *
   * @param loader   The classloader used to fetch the resource
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static URL getResourceURL(ClassLoader loader, String resource) throws IOException {
    URL url = classLoaderWrapper.getResourceAsURL(resource, loader);
    if (url == null) {
      throw new IOException("Could not find resource " + resource);
    }
    return url;
  }

  /**
   * Returns a resource on the classpath as a Stream object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static InputStream getResourceAsStream(String resource) throws IOException {
    return getResourceAsStream(null, resource);
  }

  /**
   * Returns a resource on the classpath as a Stream object
   *
   * @param loader   The classloader used to fetch the resource
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
    InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
    if (in == null) {
      throw new IOException("Could not find resource " + resource);
    }
    return in;
  }

  /**
   * Returns a resource on the classpath as a Properties object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Properties getResourceAsProperties(String resource) throws IOException {
    Properties props = new Properties();
    try (InputStream in = getResourceAsStream(resource)) {
      props.load(in);
    }
    return props;
  }

  /**
   * Returns a resource on the classpath as a Properties object
   *
   * @param loader   The classloader used to fetch the resource
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Properties getResourceAsProperties(ClassLoader loader, String resource) throws IOException {
    Properties props = new Properties();
    try (InputStream in = getResourceAsStream(loader, resource)) {
      props.load(in);
    }
    return props;
  }

  /**
   * Returns a resource on the classpath as a Reader object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Reader getResourceAsReader(String resource) throws IOException {
    Reader reader;
    if (charset == null) {
      reader = new InputStreamReader(getResourceAsStream(resource));
    } else {
      reader = new InputStreamReader(getResourceAsStream(resource), charset);
    }
    return reader;
  }

  /**
   * Returns a resource on the classpath as a Reader object
   *
   * @param loader   The classloader used to fetch the resource
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Reader getResourceAsReader(ClassLoader loader, String resource) throws IOException {
    Reader reader;
    if (charset == null) {
      reader = new InputStreamReader(getResourceAsStream(loader, resource));
    } else {
      reader = new InputStreamReader(getResourceAsStream(loader, resource), charset);
    }
    return reader;
  }

  /**
   * Returns a resource on the classpath as a File object
   *
   * @param resource The resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static File getResourceAsFile(String resource) throws IOException {
    return new File(getResourceURL(resource).getFile());
  }

  /**
   * Returns a resource on the classpath as a File object
   *
   * @param loader   - the classloader used to fetch the resource
   * @param resource - the resource to find
   * @return The resource
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static File getResourceAsFile(ClassLoader loader, String resource) throws IOException {
    return new File(getResourceURL(loader, resource).getFile());
  }

  /**
   * Gets a URL as an input stream
   *
   * @param urlString - the URL to get
   * @return An input stream with the data from the URL
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static InputStream getUrlAsStream(String urlString) throws IOException {
    URL url = new URL(urlString);
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  /**
   * Gets a URL as a Reader
   *
   * @param urlString - the URL to get
   * @return A Reader with the data from the URL
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Reader getUrlAsReader(String urlString) throws IOException {
    Reader reader;
    if (charset == null) {
      reader = new InputStreamReader(getUrlAsStream(urlString));
    } else {
      reader = new InputStreamReader(getUrlAsStream(urlString), charset);
    }
    return reader;
  }

  /**
   * Gets a URL as a Properties object
   *
   * @param urlString - the URL to get
   * @return A Properties object with the data from the URL
   * @throws java.io.IOException If the resource cannot be found or read
   */
  public static Properties getUrlAsProperties(String urlString) throws IOException {
    Properties props = new Properties();
    try (InputStream in = getUrlAsStream(urlString)) {
      props.load(in);
    }
    return props;
  }

  /**
   * Loads a class
   *
   * @param className - the class to fetch
   * @return The loaded class
   * @throws ClassNotFoundException If the class cannot be found (duh!)
   */
  public static Class<?> classForName(String className) throws ClassNotFoundException {
    return classLoaderWrapper.classForName(className);
  }

  public static Charset getCharset() {
    return charset;
  }

  public static void setCharset(Charset charset) {
    Resources.charset = charset;
  }

}
