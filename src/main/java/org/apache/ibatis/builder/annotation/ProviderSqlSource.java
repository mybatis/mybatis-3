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
package org.apache.ibatis.builder.annotation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 * @author liuzh
 */
public class ProviderSqlSource extends AbstractProviderSqlSource {

  /**
   * @deprecated Please use the {@link #ProviderSqlSource(Configuration, Object, Class, Method)} instead of this.
   */
  @Deprecated
  public ProviderSqlSource(Configuration configuration, Object provider) {
    this(configuration, provider, null, null);
  }

  /**
   * @since 3.4.5
   */
  public ProviderSqlSource(Configuration configuration, Object provider, Class<?> mapperType, Method mapperMethod) {
    super(configuration, provider, mapperType, mapperMethod);
  }

  @Override
  public SqlSource createSqlSource(Object parameterObject) {
    try {
      int bindParameterCount = providerMethodParameterTypes.length - (providerContext == null ? 0 : 1);
      String sql;
      if (providerMethodParameterTypes.length == 0) {
        sql = (String) providerMethod.invoke(providerType.newInstance());
      } else if (bindParameterCount == 0) {
        sql = (String) providerMethod.invoke(providerType.newInstance(), providerContext);
      } else if (bindParameterCount == 1 &&
              (parameterObject == null || providerMethodParameterTypes[(providerContextIndex == null || providerContextIndex == 1) ? 0 : 1].isAssignableFrom(parameterObject.getClass()))) {
        sql = (String) providerMethod.invoke(providerType.newInstance(), extractProviderMethodArguments(parameterObject));
      } else if (parameterObject instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) parameterObject;
        sql = (String) providerMethod.invoke(providerType.newInstance(), extractProviderMethodArguments(params, providerMethodArgumentNames));
      } else {
        throw new BuilderException("Error invoking SqlProvider method ("
                + providerType.getName() + "." + providerMethod.getName()
                + "). Cannot invoke a method that holds "
                + (bindParameterCount == 1 ? "named argument(@Param)": "multiple arguments")
                + " using a specifying parameterObject. In this case, please specify a 'java.util.Map' object.");
      }
      Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
      return createSqlSource(sql, parameterType, new HashMap<String, Object>());
    } catch (BuilderException e) {
      throw e;
    } catch (Exception e) {
      throw new BuilderException("Error invoking SqlProvider method ("
          + providerType.getName() + "." + providerMethod.getName()
          + ").  Cause: " + e, e);
    }
  }

  /**
   * @since 3.4.6
   */
  protected SqlSource createSqlSource(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters){
    return sqlSourceParser.parse(replacePlaceholder(originalSql), parameterType, additionalParameters);
  }

  private Object[] extractProviderMethodArguments(Object parameterObject) {
    if (providerContext != null) {
      Object[] args = new Object[2];
      args[providerContextIndex == 0 ? 1 : 0] = parameterObject;
      args[providerContextIndex] = providerContext;
      return args;
    } else {
      return new Object[] { parameterObject };
    }
  }

  private Object[] extractProviderMethodArguments(Map<String, Object> params, String[] argumentNames) {
    Object[] args = new Object[argumentNames.length];
    for (int i = 0; i < args.length; i++) {
      if (providerContextIndex != null && providerContextIndex == i) {
        args[i] = providerContext;
      } else {
        args[i] = params.get(argumentNames[i]);
      }
    }
    return args;
  }

  private String replacePlaceholder(String sql) {
    return PropertyParser.parse(sql, configuration.getVariables());
  }

}
