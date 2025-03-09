/*
 *    Copyright 2009-2025 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import ognl.OgnlContext;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import org.apache.ibatis.builder.ParameterMappingTokenHandler;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class DynamicContext {

  public static final String PARAMETER_OBJECT_KEY = "_parameter";
  public static final String DATABASE_ID_KEY = "_databaseId";

  static {
    OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
  }

  protected final ContextMap bindings;
  private final StringJoiner sqlBuilder = new StringJoiner(" ");

  private final Configuration configuration;
  private final Object parameterObject;
  private final Class<?> parameterType;
  private final boolean paramExists;

  private GenericTokenParser tokenParser;
  private ParameterMappingTokenHandler tokenHandler;

  public DynamicContext(Configuration configuration, Class<?> parameterType) {
    this(configuration, null, parameterType, false);
  }

  public DynamicContext(Configuration configuration, Object parameterObject, Class<?> parameterType,
      boolean paramExists) {
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
          configuration, parameterObject, parameterType, bindings, paramExists);
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

  protected boolean isParamExists() {
    return paramExists;
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

  static class ContextAccessor implements PropertyAccessor {

    @Override
    public Object getProperty(OgnlContext context, Object target, Object name) {
      Map map = (Map) target;

      Object result = map.get(name);
      if (map.containsKey(name) || result != null) {
        return result;
      }

      Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
      if (parameterObject instanceof Map) {
        return ((Map) parameterObject).get(name);
      }

      return null;
    }

    @Override
    public void setProperty(OgnlContext context, Object target, Object name, Object value) {
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
}
