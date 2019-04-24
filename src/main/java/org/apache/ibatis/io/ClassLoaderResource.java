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

import java.io.IOException;
import java.io.InputStream;

public class ClassLoaderResource implements Resource {

  private final String path;
  private final ClassLoader classLoader;
  private final Class<?> clazz;

  public ClassLoaderResource(String path, Class<?> clazz) {
    this(path, clazz, clazz == null ? null : clazz.getClassLoader());
  }

  public ClassLoaderResource(String path, Class<?> clazz, ClassLoader classLoader) {
    super();
    this.path = path;
    this.clazz = clazz;
    this.classLoader = classLoader;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    // #1347
    InputStream inputStream = null;
    if (clazz != null) {
      inputStream = clazz.getResourceAsStream("/" + path);
    }
    if (inputStream == null) {
      // Search XML mapper that is not in the module but in the classpath.
      inputStream = Resources.getResourceAsStream(classLoader, path);
    }
    return inputStream;
  }

  @Override
  public boolean exists() {
    if (clazz == null) {
      return Resources.exists(path, classLoader);
    }
    return clazz.getResource(path) != null || Resources.exists(path, classLoader);
  }

  @Override
  public String getLocation() {
    return path;
  }

}
