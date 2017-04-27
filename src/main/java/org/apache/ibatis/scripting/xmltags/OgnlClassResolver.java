/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.scripting.xmltags;

import java.util.HashMap;
import java.util.Map;

import ognl.ClassResolver;

import org.apache.ibatis.io.Resources;

/**
 * Custom ognl {@code ClassResolver} which behaves same like ognl's
 * {@code DefaultClassResolver}. But uses the {@code Resources}
 * utility class to find the target class instead of {@code Class#forName(String)}. 
 *
 * @author Daniel Guggi 
 *
 * @see <a href='https://github.com/mybatis/mybatis-3/issues/161'>Issue 161</a>
 */
public class OgnlClassResolver implements ClassResolver {

  private Map<String, Class<?>> classes = new HashMap<String, Class<?>>(101);

  @Override
  public Class classForName(String className, Map context) throws ClassNotFoundException {
    Class<?> result = null;
    if ((result = classes.get(className)) == null) {
      try {
        result = Resources.classForName(className);
      } catch (ClassNotFoundException e1) {
        if (className.indexOf('.') == -1) {
          result = Resources.classForName("java.lang." + className);
          classes.put("java.lang." + className, result);
        }
      }
      classes.put(className, result);
    }
    return result;
  }

}
