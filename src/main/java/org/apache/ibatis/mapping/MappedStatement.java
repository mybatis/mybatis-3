/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public final class MappedStatement {

  private final MappedStatementProperties properties;

  private MappedStatement(MappedStatementProperties properties) {
    this.properties = properties;
  }

  public static class Builder {
    private final MappedStatementProperties properties = new MappedStatementProperties();

    public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
      properties.setConfiguration(configuration);
      properties.setId(id);
      properties.setSqlSource(sqlSource);
      properties.setSqlCommandType(sqlCommandType);
      properties.setStatementType(StatementType.PREPARED);
      properties.setResultSetType(ResultSetType.DEFAULT);
      properties.setParameterMap(
          new ParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<>()).build());
      properties.setResultMaps(new ArrayList<>());
      properties.setKeyGenerator(configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType)
          ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE);
      String logId = id;
      if (configuration.getLogPrefix() != null) {
        logId = configuration.getLogPrefix() + id;
      }
      properties.setStatementLog(LogFactory.getLog(logId));
      properties.setLang(configuration.getDefaultScriptingLanguageInstance());
    }

    public Builder resource(String resource) {
      properties.setResource(resource);
      return this;
    }

    public String id() {
      return properties.getId();
    }

    public Builder parameterMap(ParameterMap parameterMap) {
      properties.setParameterMap(parameterMap);
      return this;
    }

    public Builder resultMaps(List<ResultMap> resultMaps) {
      properties.setResultMaps(resultMaps);
      for (ResultMap resultMap : resultMaps) {
        properties.setHasNestedResultMaps(properties.isHasNestedResultMaps() || resultMap.hasNestedResultMaps());
      }
      return this;
    }

    public Builder fetchSize(Integer fetchSize) {
      properties.setFetchSize(fetchSize);
      return this;
    }

    public Builder timeout(Integer timeout) {
      properties.setTimeout(timeout);
      return this;
    }

    public Builder statementType(StatementType statementType) {
      properties.setStatementType(statementType);
      return this;
    }

    public Builder resultSetType(ResultSetType resultSetType) {
      properties.setResultSetType(resultSetType == null ? ResultSetType.DEFAULT : resultSetType);
      return this;
    }

    public Builder cache(Cache cache) {
      properties.setCache(cache);
      return this;
    }

    public Builder flushCacheRequired(boolean flushCacheRequired) {
      properties.setFlushCacheRequired(flushCacheRequired);
      return this;
    }

    public Builder useCache(boolean useCache) {
      properties.setUseCache(useCache);
      return this;
    }

    public Builder resultOrdered(boolean resultOrdered) {
      properties.setResultOrdered(resultOrdered);
      return this;
    }

    public Builder keyGenerator(KeyGenerator keyGenerator) {
      properties.setKeyGenerator(keyGenerator);
      return this;
    }

    public Builder keyProperty(String keyProperty) {
      properties.setKeyProperties(delimitedStringToArray(keyProperty));
      return this;
    }

    public Builder keyColumn(String keyColumn) {
      properties.setKeyColumns(delimitedStringToArray(keyColumn));
      return this;
    }

    public Builder databaseId(String databaseId) {
      properties.setDatabaseId(databaseId);
      return this;
    }

    public Builder lang(LanguageDriver driver) {
      properties.setLang(driver);
      return this;
    }

    public Builder resultSets(String resultSet) {
      properties.setResultSets(delimitedStringToArray(resultSet));
      return this;
    }

    public Builder dirtySelect(boolean dirtySelect) {
      properties.setDirtySelect(dirtySelect);
      return this;
    }

    public MappedStatement build() {
      assert properties.getConfiguration() != null;
      assert properties.getId() != null;
      assert properties.getSqlSource() != null;
      assert properties.getLang() != null;
      properties.setResultMaps(Collections.unmodifiableList(properties.getResultMaps()));
      return new MappedStatement(properties);
    }
  }

  public KeyGenerator getKeyGenerator() {
    return properties.getKeyGenerator();
  }

  public SqlCommandType getSqlCommandType() {
    return properties.getSqlCommandType();
  }

  public String getResource() {
    return properties.getResource();
  }

  public Configuration getConfiguration() {
    return properties.getConfiguration();
  }

  public String getId() {
    return properties.getId();
  }

  public boolean hasNestedResultMaps() {
    return properties.isHasNestedResultMaps();
  }

  public Integer getFetchSize() {
    return properties.getFetchSize();
  }

  public Integer getTimeout() {
    return properties.getTimeout();
  }

  public StatementType getStatementType() {
    return properties.getStatementType();
  }

  public ResultSetType getResultSetType() {
    return properties.getResultSetType();
  }

  public SqlSource getSqlSource() {
    return properties.getSqlSource();
  }

  public ParameterMap getParameterMap() {
    return properties.getParameterMap();
  }

  public List<ResultMap> getResultMaps() {
    return properties.getResultMaps();
  }

  public Cache getCache() {
    return properties.getCache();
  }

  public boolean isFlushCacheRequired() {
    return properties.isFlushCacheRequired();
  }

  public boolean isUseCache() {
    return properties.isUseCache();
  }

  public boolean isResultOrdered() {
    return properties.isResultOrdered();
  }

  public String getDatabaseId() {
    return properties.getDatabaseId();
  }

  public String[] getKeyProperties() {
    return properties.getKeyProperties();
  }

  public String[] getKeyColumns() {
    return properties.getKeyColumns();
  }

  public Log getStatementLog() {
    return properties.getStatementLog();
  }

  public LanguageDriver getLang() {
    return properties.getLang();
  }

  public String[] getResultSets() {
    return properties.getResultSets();
  }

  public boolean isDirtySelect() {
    return properties.isDirtySelect();
  }

  public BoundSql getBoundSql(Object parameterObject) {
    BoundSql boundSql = properties.getSqlSource().getBoundSql(parameterObject);
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings == null || parameterMappings.isEmpty()) {
      boundSql = new BoundSql(properties.getConfiguration(), boundSql.getSql(),
          properties.getParameterMap().getParameterMappings(), parameterObject);
    }

    for (ParameterMapping pm : boundSql.getParameterMappings()) {
      String rmId = pm.getResultMapId();
      if (rmId != null) {
        ResultMap rm = properties.getConfiguration().getResultMap(rmId);
        if (rm != null) {
          properties.setHasNestedResultMaps(properties.isHasNestedResultMaps() || rm.hasNestedResultMaps());
        }
      }
    }

    return boundSql;
  }

  private static String[] delimitedStringToArray(String in) {
    if (in == null || in.trim().length() == 0) {
      return null;
    }
    return in.split(",");
  }
}
