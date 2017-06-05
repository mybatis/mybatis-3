/**
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
// TODO: 17/4/18 by zmyer
public final class MappedStatement {
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

    // TODO: 17/4/18 by zmyer
    MappedStatement() {
        // constructor disabled
    }

    // TODO: 17/4/18 by zmyer
    public static class Builder {
        //创建Map Statement对象
        private MappedStatement mappedStatement = new MappedStatement();

        // TODO: 17/4/18 by zmyer
        public Builder(Configuration configuration, String id, SqlSource sqlSource,
            SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.statementType = StatementType.PREPARED;
            mappedStatement.parameterMap = new ParameterMap.Builder(configuration, "defaultParameterMap", null, new ArrayList<ParameterMapping>()).build();
            mappedStatement.resultMaps = new ArrayList<ResultMap>();
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() &&
                SqlCommandType.INSERT.equals(sqlCommandType) ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            String logId = id;
            if (configuration.getLogPrefix() != null) {
                logId = configuration.getLogPrefix() + id;
            }
            mappedStatement.statementLog = LogFactory.getLog(logId);
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        // TODO: 17/4/18 by zmyer
        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public String id() {
            return mappedStatement.id;
        }

        // TODO: 17/4/18 by zmyer
        public Builder parameterMap(ParameterMap parameterMap) {
            mappedStatement.parameterMap = parameterMap;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            for (ResultMap resultMap : resultMaps) {
                mappedStatement.hasNestedResultMaps = mappedStatement.hasNestedResultMaps || resultMap.hasNestedResultMaps();
            }
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder fetchSize(Integer fetchSize) {
            mappedStatement.fetchSize = fetchSize;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder timeout(Integer timeout) {
            mappedStatement.timeout = timeout;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder statementType(StatementType statementType) {
            mappedStatement.statementType = statementType;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder resultSetType(ResultSetType resultSetType) {
            mappedStatement.resultSetType = resultSetType;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder resultOrdered(boolean resultOrdered) {
            mappedStatement.resultOrdered = resultOrdered;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder keyColumn(String keyColumn) {
            mappedStatement.keyColumns = delimitedStringToArray(keyColumn);
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder databaseId(String databaseId) {
            mappedStatement.databaseId = databaseId;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder lang(LanguageDriver driver) {
            mappedStatement.lang = driver;
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public Builder resultSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }

        // TODO: 17/4/18 by zmyer

        /** @deprecated Use {@link #resultSets} */
        @Deprecated
        public Builder resulSets(String resultSet) {
            mappedStatement.resultSets = delimitedStringToArray(resultSet);
            return this;
        }

        // TODO: 17/4/18 by zmyer
        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            assert mappedStatement.sqlSource != null;
            assert mappedStatement.lang != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }
    }

    // TODO: 17/4/18 by zmyer
    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    // TODO: 17/4/18 by zmyer
    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    // TODO: 17/4/18 by zmyer
    public String getResource() {
        return resource;
    }

    // TODO: 17/4/18 by zmyer
    public Configuration getConfiguration() {
        return configuration;
    }

    // TODO: 17/4/18 by zmyer
    public String getId() {
        return id;
    }

    // TODO: 17/4/18 by zmyer
    public boolean hasNestedResultMaps() {
        return hasNestedResultMaps;
    }

    // TODO: 17/4/18 by zmyer
    public Integer getFetchSize() {
        return fetchSize;
    }

    // TODO: 17/4/18 by zmyer
    public Integer getTimeout() {
        return timeout;
    }

    // TODO: 17/4/18 by zmyer
    public StatementType getStatementType() {
        return statementType;
    }

    // TODO: 17/4/18 by zmyer
    public ResultSetType getResultSetType() {
        return resultSetType;
    }

    // TODO: 17/4/18 by zmyer
    public SqlSource getSqlSource() {
        return sqlSource;
    }

    // TODO: 17/4/18 by zmyer
    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    // TODO: 17/4/18 by zmyer
    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    // TODO: 17/4/18 by zmyer
    public Cache getCache() {
        return cache;
    }

    // TODO: 17/4/18 by zmyer
    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }

    // TODO: 17/4/18 by zmyer
    public boolean isUseCache() {
        return useCache;
    }

    // TODO: 17/4/18 by zmyer
    public boolean isResultOrdered() {
        return resultOrdered;
    }

    // TODO: 17/4/18 by zmyer
    public String getDatabaseId() {
        return databaseId;
    }

    // TODO: 17/4/18 by zmyer
    public String[] getKeyProperties() {
        return keyProperties;
    }

    // TODO: 17/4/18 by zmyer
    public String[] getKeyColumns() {
        return keyColumns;
    }

    // TODO: 17/4/18 by zmyer
    public Log getStatementLog() {
        return statementLog;
    }

    // TODO: 17/4/18 by zmyer
    public LanguageDriver getLang() {
        return lang;
    }

    // TODO: 17/4/18 by zmyer
    public String[] getResultSets() {
        return resultSets;
    }

    /** @deprecated Use {@link #getResultSets()} */
    @Deprecated
    public String[] getResulSets() {
        return resultSets;
    }

    // TODO: 17/4/18 by zmyer
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }

        // check for nested result maps in parameter mappings (issue #30)
        for (ParameterMapping pm : boundSql.getParameterMappings()) {
            String rmId = pm.getResultMapId();
            if (rmId != null) {
                ResultMap rm = configuration.getResultMap(rmId);
                if (rm != null) {
                    hasNestedResultMaps |= rm.hasNestedResultMaps();
                }
            }
        }

        return boundSql;
    }

    // TODO: 17/4/18 by zmyer
    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }

}
