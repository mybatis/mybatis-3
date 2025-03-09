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
package org.apache.ibatis.scripting.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.builder.ParameterMappingTokenHandler;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * Static SqlSource. It is faster than {@link DynamicSqlSource} because mappings are calculated during startup.
 *
 * @since 3.2.0
 *
 * @author Eduardo Macarron
 */
public class RawSqlSource implements SqlSource {

  private final SqlSource sqlSource;

  public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
    DynamicContext context = new DynamicContext(configuration, parameterType);
    rootSqlNode.apply(context);
    String sql = context.getSql();
    sqlSource = SqlSourceBuilder.buildSqlSource(configuration, sql, context.getParameterMappings());
  }

  public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
    Class<?> clazz = parameterType == null ? Object.class : parameterType;
    List<ParameterMapping> parameterMappings = new ArrayList<>();
    ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler(parameterMappings, configuration,
        clazz, new HashMap<>());
    GenericTokenParser parser = new GenericTokenParser("#{", "}", tokenHandler);
    sqlSource = SqlSourceBuilder.buildSqlSource(configuration, parser.parse(sql), parameterMappings);
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return sqlSource.getBoundSql(parameterObject);
  }

}
