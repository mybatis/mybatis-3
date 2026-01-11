/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.apache.ibatis.scripting.xmltags;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ognl.DefaultClassResolver;
import ognl.MemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.scripting.xmltags.DynamicContext.ContextMap;

/**
 * Evaluate expression using OGNL.<br>
 * Parsed expressions will be cached for better performance.
 *
 * @author Eduardo Macarron
 *
 * @see <a href='https://github.com/mybatis/old-google-code-issues/issues/342'>Issue 342</a>
 */
public class OgnlExpressionParser implements ExpressionParser {

  private static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();
  private static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();
  private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

  static {
    OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
  }

  @Override
  public Object getValue(String expression, Object root) {
    try {
      OgnlContext context = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
      return Ognl.getValue(parseExpression(expression), context, root);
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
  }

  private Object parseExpression(String expression) throws OgnlException {
    try {
      return expressionCache.computeIfAbsent(expression, k -> {
        try {
          return Ognl.parseExpression(k);
        } catch (OgnlException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException re) {
      Throwable cause = re.getCause();
      if (cause instanceof OgnlException) {
        throw (OgnlException) cause;
      }
      throw re;
    }
  }

  static class ContextAccessor implements PropertyAccessor {
    @SuppressWarnings("rawtypes")
    @Override
    public Object getProperty(OgnlContext context, Object target, Object name) {
      Map map = (Map) target;

      Object result = map.get(name);
      if (map.containsKey(name) || result != null) {
        return result;
      }

      Object parameterObject = map.get(DynamicContext.PARAMETER_OBJECT_KEY);
      if (parameterObject instanceof Map) {
        return ((Map) parameterObject).get(name);
      }

      return null;
    }

    @Override
    public void setProperty(OgnlContext context, Object target, Object name, Object value) {
      @SuppressWarnings("unchecked")
      Map<Object, Object> map = (Map<Object, Object>) target;
      map.put(name, value);
    }

    @Override
    public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
      return null;
    }

    @Override
    public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
      return null;
    }
  }

  /**
   * Custom ognl {@code ClassResolver} which behaves same like ognl's {@code DefaultClassResolver}. But uses the
   * {@code Resources} utility class to find the target class instead of {@code Class#forName(String)}.
   *
   * @author Daniel Guggi
   *
   * @see <a href='https://github.com/mybatis/mybatis-3/issues/161'>Issue 161</a>
   */
  static class OgnlClassResolver extends DefaultClassResolver {
    @Override
    protected Class toClassForName(String className) throws ClassNotFoundException {
      return Resources.classForName(className);
    }
  }

  /**
   * The {@link MemberAccess} class that based on <a href=
   * 'https://github.com/jkuhnert/ognl/blob/OGNL_3_2_1/src/java/ognl/DefaultMemberAccess.java'>DefaultMemberAccess</a>.
   *
   * @author Kazuki Shimizu
   *
   * @since 3.5.0
   *
   * @see <a href=
   *      'https://github.com/jkuhnert/ognl/blob/OGNL_3_2_1/src/java/ognl/DefaultMemberAccess.java'>DefaultMemberAccess</a>
   * @see <a href='https://github.com/jkuhnert/ognl/issues/47'>#47 of ognl</a>
   */
  static class OgnlMemberAccess implements MemberAccess {

    private final boolean canControlMemberAccessible;

    OgnlMemberAccess() {
      this.canControlMemberAccessible = Reflector.canControlMemberAccessible();
    }

    @Override
    public Object setup(OgnlContext context, Object target, Member member, String propertyName) {
      Object result = null;
      if (isAccessible(context, target, member, propertyName)) {
        AccessibleObject accessible = (AccessibleObject) member;
        if (!accessible.canAccess(target)) {
          result = Boolean.FALSE;
          accessible.setAccessible(true);
        }
      }
      return result;
    }

    @Override
    public void restore(OgnlContext context, Object target, Member member, String propertyName, Object state) {
      // Flipping accessible flag is not thread safe. See #1648
    }

    @Override
    public boolean isAccessible(OgnlContext context, Object target, Member member, String propertyName) {
      return canControlMemberAccessible;
    }

  }
}
