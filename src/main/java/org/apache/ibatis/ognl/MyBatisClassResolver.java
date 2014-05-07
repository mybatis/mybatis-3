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

package org.apache.ibatis.ognl;

import ognl.ClassResolver;

import java.util.HashMap;
import java.util.Map;

/*
 * Custom ognl {@code ClassResolver} which behaves same like ognl's {@code DefaultClassResolver}. But
 * additionally tries to find the class using the current thread's context-class-loader if not
 * found via {@code Class.forName}.
 *
 * @see https://github.com/mybatis/mybatis-3/issues/161
 *
 * @author Daniel Guggi
 */
public class MyBatisClassResolver implements ClassResolver {

    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>(101);

    public MyBatisClassResolver() {
        super();
    }

    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result = null;

        if ((result = (Class)classes.get(className)) == null) {
            try {
                result = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                try {
                    result = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e2) {
                    if (className.indexOf('.') == -1) {
                        result = Class.forName("java.lang." + className);
                        classes.put("java.lang." + className, result);
                    }
                }
            }
            classes.put(className, result);
        }
        return result;
    }
}
