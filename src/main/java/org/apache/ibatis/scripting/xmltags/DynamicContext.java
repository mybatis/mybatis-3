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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.ParameterMappingTokenHandler;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class DynamicContext {

  public static final String PARAMETER_OBJECT_KEY = "_parameter";
  public static final String DATABASE_ID_KEY = "_databaseId";

  protected final ContextMap bindings;
  private final StringJoiner sqlBuilder = new StringJoiner(" ");

  private final Configuration configuration;
  private final Object parameterObject;
  private final Class<?> parameterType;
  private final ParamNameResolver paramNameResolver;
  private final boolean paramExists;
  private final ExpressionParser expressionParser;

  private GenericTokenParser tokenParser;
  private ParameterMappingTokenHandler tokenHandler;

  public DynamicContext(Configuration configuration, Class<?> parameterType, ParamNameResolver paramNameResolver) {
    this(configuration, null, parameterType, paramNameResolver, false);
  }

  public DynamicContext(Configuration configuration, Object parameterObject, Class<?> parameterType,
      ParamNameResolver paramNameResolver, boolean paramExists) {
    if (parameterObject == null || parameterObject instanceof Map) {
      bindings = new ContextMap(null, false);
    } else {
      MetaObject metaObject = configuration.newMetaObject(parameterObject);
      boolean existsTypeHandler = configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
      bindings = new ContextMap(metaObject, existsTypeHandler);
    }
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
    this.configuration = configuration;
    this.parameterObject = parameterObject;
    this.paramExists = paramExists;
    this.parameterType = parameterType;
    this.paramNameResolver = paramNameResolver;
    this.expressionParser = configuration.getExpressionParser();
  }

  public Map<String, Object> getBindings() {
    return bindings;
  }

  public void bind(String name, Object value) {
    bindings.put(name, value);
  }

  public void appendSql(String sql) {
    sqlBuilder.add(sql);
  }

  public String getSql() {
    return sqlBuilder.toString().trim();
  }

  private void initTokenParser(List<ParameterMapping> parameterMappings) {
    if (tokenParser == null) {
      tokenHandler = new ParameterMappingTokenHandler(parameterMappings != null ? parameterMappings : new ArrayList<>(),
          configuration, parameterObject, parameterType, bindings, paramNameResolver, paramExists);
      tokenParser = new GenericTokenParser("#{", "}", tokenHandler);
    }
  }

  public List<ParameterMapping> getParameterMappings() {
    initTokenParser(null);
    return tokenHandler.getParameterMappings();
  }

  protected String parseParam(String sql) {
    initTokenParser(getParameterMappings());
    return tokenParser.parse(sql);
  }

  protected Object getParameterObject() {
    return parameterObject;
  }

  protected Class<?> getParameterType() {
    return parameterType;
  }

  protected ParamNameResolver getParamNameResolver() {
    return paramNameResolver;
  }

  protected boolean isParamExists() {
    return paramExists;
  }

  public Object getValue(String expression) {
    return expressionParser.getValue(expression, getBindings());
  }

  public boolean evaluateBoolean(String expression) {
    Object value = expressionParser.getValue(expression, getBindings());
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) {
      return new BigDecimal(String.valueOf(value)).compareTo(BigDecimal.ZERO) != 0;
    }
    return value != null;
  }

  @SuppressWarnings("rawtypes")
  public Iterable<?> evaluateIterable(String expression, boolean nullable) {
    Object value = expressionParser.getValue(expression, getBindings());
    if (value == null) {
      if (nullable) {
        return null;
      }
      throw new BuilderException("The expression '" + expression + "' evaluated to a null value.");
    }
    if (value instanceof Iterable) {
      return (Iterable<?>) value;
    }
    if (value.getClass().isArray()) {
      // the array may be primitive, so Arrays.asList() may throw
      // a ClassCastException (issue 209). Do the work manually
      // Curse primitives! :) (JGB)
      int size = Array.getLength(value);
      List<Object> answer = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        Object o = Array.get(value, i);
        answer.add(o);
      }
      return answer;
    }
    if (value instanceof Map) {
      return ((Map) value).entrySet();
    }
    throw new BuilderException(
        "Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
  }

  static class ContextMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 2977601501966151582L;
    private final MetaObject parameterMetaObject;
    private final boolean fallbackParameterObject;

    public ContextMap(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
      this.parameterMetaObject = parameterMetaObject;
      this.fallbackParameterObject = fallbackParameterObject;
    }

    @Override
    public Object get(Object key) {
      String strKey = (String) key;
      if (super.containsKey(strKey)) {
        return super.get(strKey);
      }

      if (parameterMetaObject == null) {
        return null;
      }

      if (fallbackParameterObject && !parameterMetaObject.hasGetter(strKey)) {
        return parameterMetaObject.getOriginalObject();
      }
      // issue #61 do not modify the context when reading
      return parameterMetaObject.getValue(strKey);
    }
  }
}
