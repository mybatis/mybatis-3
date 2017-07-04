/**
 *    Copyright 2009-2017 the original author or authors.
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

package org.apache.ibatis.reflection;

import org.apache.ibatis.io.Resources;

/**
 * To check the existence of version dependent classes.
 */
public class Jdk {

  /**
   * <code>true</code> if <code>java.lang.reflect.Parameter</code> is available.
   */
  public static final boolean parameterExists;

  static {
    boolean available = false;
    try {
      Resources.classForName("java.lang.reflect.Parameter");
      available = true;
    } catch (ClassNotFoundException e) {
      // ignore
    }
    parameterExists = available;
  }

  public static final boolean dateAndTimeApiExists;

  static {
    boolean available = false;
    try {
      Resources.classForName("java.time.Clock");
      available = true;
    } catch (ClassNotFoundException e) {
      // ignore
    }
    dateAndTimeApiExists = available;
  }

  private Jdk() {
    super();
  }
}
