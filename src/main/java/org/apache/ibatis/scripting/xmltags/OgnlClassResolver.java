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
package org.apache.ibatis.scripting.xmltags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.ClassResolver;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeException;

/**
 * Custom ognl {@code ClassResolver} which behaves same like ognl's
 * {@code DefaultClassResolver}. But uses the {@link TypeAliasRegistry#resolveAlias(String)}
 * utility class to find the target class instead of {@code Class#forName(String)}.
 *
 * @author Daniel Guggi
 *
 * @see <a href='https://github.com/mybatis/mybatis-3/issues/161'>Issue 161</a>
 */
public class OgnlClassResolver implements ClassResolver {

  private final Map<String, Class> classCache = new ConcurrentHashMap<>(101);
  private final TypeAliasRegistry registry;

  public OgnlClassResolver() {
    this(new TypeAliasRegistry());
  }

  /**
   * @since 3.5.2
   */
  public OgnlClassResolver(TypeAliasRegistry registry) {
    this.registry = registry;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class classForName(String className, Map context) {
    return classCache.computeIfAbsent(className, k -> {
      try {
        return registry.resolveAlias(k);
      } catch (TypeException e) {
        if (k.indexOf('.') == -1) {
          // fallback to resolve class from 'java.lang' package (support same behavior with DefaultClassResolver)
          return registry.resolveAlias("java.lang." + className);
        }
        throw e;
      }
    });
  }

}
