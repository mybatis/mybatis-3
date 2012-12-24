/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.binding;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

public class MapperRegistry {

  private Configuration config;
  private final Map<Class<?>, Map<Method, MapperMethod>> knownMappers = new HashMap<Class<?>, Map<Method, MapperMethod>>();

  public MapperRegistry(Configuration config) {
    this.config = config;
  }

  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    if (!knownMappers.containsKey(type))
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    try {
      return newMapperProxy(type, sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }

  protected <T> T newMapperProxy(Class<T> type, SqlSession sqlSession) {
    final Map<Method, MapperMethod> methodCache = knownMappers.get(type);
    return MapperProxy.newMapperProxy(type, sqlSession, methodCache);
  }

  public boolean hasMapper(Class<?> type) {
    return knownMappers.containsKey(type);
  }

  public void addMapper(Class<?> type) {
    if (type.isInterface()) {
      if (hasMapper(type)) {
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
        knownMappers.put(type, new ConcurrentHashMap<Method, MapperMethod>());
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser.  If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
  
}
