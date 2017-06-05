/**
 * Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.session.defaults;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * The default implementation for {@link SqlSession}.
 * Note that this class is not Thread-Safe.
 *
 * @author Clinton Begin
 */
// TODO: 17/4/18 by zmyer
public class DefaultSqlSession implements SqlSession {
    //配置对象
    private Configuration configuration;
    //执行器
    private Executor executor;
    //是否自动提交事务
    private boolean autoCommit;
    //脏标记
    private boolean dirty;
    //游标集合
    private List<Cursor<?>> cursorList;

    // TODO: 17/4/18 by zmyer
    public DefaultSqlSession(Configuration configuration, Executor executor,
        boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }

    // TODO: 17/4/18 by zmyer
    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this(configuration, executor, false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // Popular vote was to return null on 0 results and throw exception on too many.
        List<T> list = this.selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <K, V> Map<K, V> selectMap(String statement, String mapKey) {
        return this.selectMap(statement, null, mapKey, RowBounds.DEFAULT);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return this.selectMap(statement, parameter, mapKey, RowBounds.DEFAULT);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey,
        RowBounds rowBounds) {
        //首先根据提供的statement和参数值,查询结果集
        final List<? extends V> list = selectList(statement, parameter, rowBounds);
        //根据查询键值以及配置对象中的工厂对象,创建默认的结果集处理对象
        final DefaultMapResultHandler<K, V> mapResultHandler = new DefaultMapResultHandler<K, V>(mapKey,
            configuration.getObjectFactory(), configuration.getObjectWrapperFactory(),
            configuration.getReflectorFactory());
        //创建结果集处理上下文对象
        final DefaultResultContext<V> context = new DefaultResultContext<V>();
        for (V o : list) {
            //依次处理每个结果对象
            context.nextResultObject(o);
            //处理结果对象
            mapResultHandler.handleResult(context);
        }
        //返回结果集
        return mapResultHandler.getMappedResults();
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <T> Cursor<T> selectCursor(String statement) {
        return selectCursor(statement, null);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter) {
        return selectCursor(statement, parameter, RowBounds.DEFAULT);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <T> Cursor<T> selectCursor(String statement, Object parameter, RowBounds rowBounds) {
        try {
            //首先根据提供的statement对象,查询注册的Mapper对象
            MappedStatement ms = configuration.getMappedStatement(statement);
            //根据Mapper对象以及参数,查询cursor对象
            Cursor<T> cursor = executor.queryCursor(ms, wrapCollection(parameter), rowBounds);
            //注册cursor对象
            registerCursor(cursor);
            //返回cursor
            return cursor;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <E> List<E> selectList(String statement) {
        return this.selectList(statement, null);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            //根据statement,查询注册的Mapper statement对象
            MappedStatement ms = configuration.getMappedStatement(statement);
            //开始根据mapper statement以及参数信息,查询结果集
            return executor.query(ms, wrapCollection(parameter), rowBounds, Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void select(String statement, Object parameter, ResultHandler handler) {
        select(statement, parameter, RowBounds.DEFAULT, handler);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void select(String statement, ResultHandler handler) {
        select(statement, null, RowBounds.DEFAULT, handler);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds,
        ResultHandler handler) {
        try {
            //查询Mapper Statement对象
            MappedStatement ms = configuration.getMappedStatement(statement);
            //根据参数,查询结果集
            executor.query(ms, wrapCollection(parameter), rowBounds, handler);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public int update(String statement) {
        return update(statement, null);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public int update(String statement, Object parameter) {
        try {
            //设置脏标记
            dirty = true;
            //从配合对象中读取Mapper Statement对象
            MappedStatement ms = configuration.getMappedStatement(statement);
            //开始执行update
            return executor.update(ms, wrapCollection(parameter));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public int delete(String statement) {
        return update(statement, null);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void commit() {
        commit(false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void commit(boolean force) {
        try {
            //执行器开始提交事务
            executor.commit(isCommitOrRollbackRequired(force));
            //事务提交完毕,清除脏标记
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void rollback() {
        rollback(false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void rollback(boolean force) {
        try {
            //执行器回滚事务
            executor.rollback(isCommitOrRollbackRequired(force));
            //清理脏标记
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public List<BatchResult> flushStatements() {
        try {
            //执行器刷新statement
            return executor.flushStatements();
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error flushing statements.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void close() {
        try {
            //关闭执行器
            executor.close(isCommitOrRollbackRequired(false));
            closeCursors();
            dirty = false;
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    private void closeCursors() {
        if (cursorList != null && cursorList.size() != 0) {
            for (Cursor<?> cursor : cursorList) {
                try {
                    cursor.close();
                } catch (IOException e) {
                    throw ExceptionFactory.wrapException("Error closing cursor.  Cause: " + e, e);
                }
            }
            cursorList.clear();
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public Connection getConnection() {
        try {
            //返回事务连接对象
            return executor.getTransaction().getConnection();
        } catch (SQLException e) {
            throw ExceptionFactory.wrapException("Error getting a new connection.  Cause: " + e, e);
        }
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public void clearCache() {
        executor.clearLocalCache();
    }

    // TODO: 17/4/18 by zmyer
    private <T> void registerCursor(Cursor<T> cursor) {
        if (cursorList == null) {
            cursorList = new ArrayList<Cursor<?>>();
        }
        cursorList.add(cursor);
    }

    // TODO: 17/4/18 by zmyer
    private boolean isCommitOrRollbackRequired(boolean force) {
        return (!autoCommit && dirty) || force;
    }

    // TODO: 17/4/18 by zmyer
    private Object wrapCollection(final Object object) {
        if (object instanceof Collection) {
            StrictMap<Object> map = new StrictMap<Object>();
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }
            return map;
        } else if (object != null && object.getClass().isArray()) {
            StrictMap<Object> map = new StrictMap<Object>();
            map.put("array", object);
            return map;
        }
        return object;
    }

    // TODO: 17/4/18 by zmyer
    public static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -5741767162221585340L;
        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
            }
            return super.get(key);
        }

    }

}
