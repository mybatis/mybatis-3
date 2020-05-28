/**
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * Provides a very simple API for accessing resources within an application server.
 *
 * @author Ben Gunter
 */
public abstract class VFS {
  private static final Log log = LogFactory.getLog(VFS.class);

  /** The built-in implementations. */
  public static final Class<?>[] IMPLEMENTATIONS = { JBoss6VFS.class, DefaultVFS.class };

  /**
   * The list to which implementations are added by {@link #addImplClass(Class)}.
   */
  public static final List<Class<? extends VFS>> USER_IMPLEMENTATIONS = new ArrayList<>();

  /** Singleton instance holder. */
  private static class VFSHolder {
    static final VFS INSTANCE = createVFS();

    @SuppressWarnings("unchecked")
    static VFS createVFS() {
      // Try the user implementations first, then the built-ins
      List<Class<? extends VFS>> impls = new ArrayList<>();
      impls.addAll(USER_IMPLEMENTATIONS);
      impls.addAll(Arrays.asList((Class<? extends VFS>[]) IMPLEMENTATIONS));

      // Try each implementation class until a valid one is found
      VFS vfs = null;
      for (int i = 0; vfs == null || !vfs.isValid(); i++) {
        Class<? extends VFS> impl = impls.get(i);
        try {
          vfs = impl.getDeclaredConstructor().newInstance();
          if (!vfs.isValid() && log.isDebugEnabled()) {
            log.debug("VFS implementation " + impl.getName()
                + " is not valid in this environment.");
          }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
          log.error("Failed to instantiate " + impl, e);
          return null;
        }
      }

      if (log.isDebugEnabled()) {
        log.debug("Using VFS adapter " + vfs.getClass().getName());
      }

      return vfs;
    }
  }

  /**
   * Get the singleton {@link VFS} instance. If no {@link VFS} implementation can be found for the current environment,
   * then this method returns null.
   *
   * @return single instance of VFS
   */
  public static VFS getInstance() {
    return VFSHolder.INSTANCE;
  }

  /**
   * Adds the specified class to the list of {@link VFS} implementations. Classes added in this
   * manner are tried in the order they are added and before any of the built-in implementations.
   *
   * @param clazz The {@link VFS} implementation class to add.
   */
  public static void addImplClass(Class<? extends VFS> clazz) {
    if (clazz != null) {
      USER_IMPLEMENTATIONS.add(clazz);
    }
  }

  /**
   * Get a class by name. If the class is not found then return null.
   *
   * @param className
   *          the class name
   * @return the class
   */
  protected static Class<?> getClass(String className) {
    try {
      return Thread.currentThread().getContextClassLoader().loadClass(className);
      // return ReflectUtil.findClass(className);
    } catch (ClassNotFoundException e) {
      if (log.isDebugEnabled()) {
        log.debug("Class not found: " + className);
      }
      return null;
    }
  }

  /**
   * Get a method by name and parameter types. If the method is not found then return null.
   *
   * @param clazz
   *          The class to which the method belongs.
   * @param methodName
   *          The name of the method.
   * @param parameterTypes
   *          The types of the parameters accepted by the method.
   * @return the method
   */
  protected static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
    if (clazz == null) {
      return null;
    }
    try {
      return clazz.getMethod(methodName, parameterTypes);
    } catch (SecurityException e) {
      log.error("Security exception looking for method " + clazz.getName() + "." + methodName + ".  Cause: " + e);
      return null;
    } catch (NoSuchMethodException e) {
      log.error("Method not found " + clazz.getName() + "." + methodName + "." + methodName + ".  Cause: " + e);
      return null;
    }
  }

  /**
   * Invoke a method on an object and return whatever it returns.
   *
   * @param <T>
   *          the generic type
   * @param method
   *          The method to invoke.
   * @param object
   *          The instance or class (for static methods) on which to invoke the method.
   * @param parameters
   *          The parameters to pass to the method.
   * @return Whatever the method returns.
   * @throws IOException
   *           If I/O errors occur
   * @throws RuntimeException
   *           If anything else goes wrong
   */
  @SuppressWarnings("unchecked")
  protected static <T> T invoke(Method method, Object object, Object... parameters)
      throws IOException, RuntimeException {
    try {
      return (T) method.invoke(object, parameters);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof IOException) {
        throw (IOException) e.getTargetException();
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Get a list of {@link URL}s from the context classloader for all the resources found at the
   * specified path.
   *
   * @param path The resource path.
   * @return A list of {@link URL}s, as returned by {@link ClassLoader#getResources(String)}.
   * @throws IOException If I/O errors occur
   */
  protected static List<URL> getResources(String path) throws IOException {
    return Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
  }

  /**
   * Return true if the {@link VFS} implementation is valid for the current environment.
   *
   * @return true, if is valid
   */
  public abstract boolean isValid();

  /**
   * Recursively list the full resource path of all the resources that are children of the
   * resource identified by a URL.
   *
   * @param url The URL that identifies the resource to list.
   * @param forPath The path to the resource that is identified by the URL. Generally, this is the
   *            value passed to {@link #getResources(String)} to get the resource URL.
   * @return A list containing the names of the child resources.
   * @throws IOException If I/O errors occur
   */
  protected abstract List<String> list(URL url, String forPath) throws IOException;

  /**
   * Recursively list the full resource path of all the resources that are children of all the
   * resources found at the specified path.
   *
   * @param path The path of the resource(s) to list.
   * @return A list containing the names of the child resources.
   * @throws IOException If I/O errors occur
   */
  public List<String> list(String path) throws IOException {
    List<String> names = new ArrayList<>();
    for (URL url : getResources(path)) {
      names.addAll(list(url, path));
    }
    return names;
  }
}
