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

import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

public class MappedStatementProperties {

  private String resource;
  private Configuration configuration;
  private String id;
  private Integer fetchSize;
  private Integer timeout;
  private StatementType statementType;
  private ResultSetType resultSetType;
  private SqlSource sqlSource;
  private Cache cache;
  private ParameterMap parameterMap;
  private List<ResultMap> resultMaps;
  private boolean flushCacheRequired;
  private boolean useCache;
  private boolean resultOrdered;
  private SqlCommandType sqlCommandType;
  private KeyGenerator keyGenerator;
  private String[] keyProperties;
  private String[] keyColumns;
  private boolean hasNestedResultMaps;
  private String databaseId;
  private Log statementLog;
  private LanguageDriver lang;
  private String[] resultSets;
  private boolean dirtySelect;

  // Getters and setters for all properties

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getFetchSize() {
    return fetchSize;
  }

  public void setFetchSize(Integer fetchSize) {
    this.fetchSize = fetchSize;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public StatementType getStatementType() {
    return statementType;
  }

  public void setStatementType(StatementType statementType) {
    this.statementType = statementType;
  }

  public ResultSetType getResultSetType() {
    return resultSetType;
  }

  public void setResultSetType(ResultSetType resultSetType) {
    this.resultSetType = resultSetType;
  }

  public SqlSource getSqlSource() {
    return sqlSource;
  }

  public void setSqlSource(SqlSource sqlSource) {
    this.sqlSource = sqlSource;
  }

  public Cache getCache() {
    return cache;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  public ParameterMap getParameterMap() {
    return parameterMap;
  }

  public void setParameterMap(ParameterMap parameterMap) {
    this.parameterMap = parameterMap;
  }

  public List<ResultMap> getResultMaps() {
    return resultMaps;
  }

  public void setResultMaps(List<ResultMap> resultMaps) {
    this.resultMaps = resultMaps;
  }

  public boolean isFlushCacheRequired() {
    return flushCacheRequired;
  }

  public void setFlushCacheRequired(boolean flushCacheRequired) {
    this.flushCacheRequired = flushCacheRequired;
  }

  public boolean isUseCache() {
    return useCache;
  }

  public void setUseCache(boolean useCache) {
    this.useCache = useCache;
  }

  public boolean isResultOrdered() {
    return resultOrdered;
  }

  public void setResultOrdered(boolean resultOrdered) {
    this.resultOrdered = resultOrdered;
  }

  public SqlCommandType getSqlCommandType() {
    return sqlCommandType;
  }

  public void setSqlCommandType(SqlCommandType sqlCommandType) {
    this.sqlCommandType = sqlCommandType;
  }

  public KeyGenerator getKeyGenerator() {
    return keyGenerator;
  }

  public void setKeyGenerator(KeyGenerator keyGenerator) {
    this.keyGenerator = keyGenerator;
  }

  public String[] getKeyProperties() {
    return keyProperties;
  }

  public void setKeyProperties(String[] keyProperties) {
    this.keyProperties = keyProperties;
  }

  public String[] getKeyColumns() {
    return keyColumns;
  }

  public void setKeyColumns(String[] keyColumns) {
    this.keyColumns = keyColumns;
  }

  public boolean isHasNestedResultMaps() {
    return hasNestedResultMaps;
  }

  public void setHasNestedResultMaps(boolean hasNestedResultMaps) {
    this.hasNestedResultMaps = hasNestedResultMaps;
  }

  public String getDatabaseId() {
    return databaseId;
  }

  public void setDatabaseId(String databaseId) {
    this.databaseId = databaseId;
  }

  public Log getStatementLog() {
    return statementLog;
  }

  public void setStatementLog(Log statementLog) {
    this.statementLog = statementLog;
  }

  public LanguageDriver getLang() {
    return lang;
  }

  public void setLang(LanguageDriver lang) {
    this.lang = lang;
  }

  public String[] getResultSets() {
    return resultSets;
  }

  public void setResultSets(String[] resultSets) {
    this.resultSets = resultSets;
  }

  public boolean isDirtySelect() {
    return dirtySelect;
  }

  public void setDirtySelect(boolean dirtySelect) {
    this.dirtySelect = dirtySelect;
  }
}
