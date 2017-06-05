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
package org.apache.ibatis.executor;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Jeff Butler
 */
// TODO: 17/4/20 by zmyer
public class BatchExecutor extends BaseExecutor {
    //
    public static final int BATCH_UPDATE_RETURN_VALUE = Integer.MIN_VALUE + 1002;
    //statement集合
    private final List<Statement> statementList = new ArrayList<Statement>();
    //结果集
    private final List<BatchResult> batchResultList = new ArrayList<BatchResult>();
    //当前的sql
    private String currentSql;
    //当前的statement
    private MappedStatement currentStatement;

    // TODO: 17/4/20 by zmyer
    public BatchExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    // TODO: 17/4/20 by zmyer
    @Override
    public int doUpdate(MappedStatement ms, Object parameterObject) throws SQLException {
        //读取配置对象
        final Configuration configuration = ms.getConfiguration();
        //创建statement处理对象
        final StatementHandler handler = configuration.newStatementHandler(this, ms,
            parameterObject, RowBounds.DEFAULT, null, null);
        final BoundSql boundSql = handler.getBoundSql();
        //读取sql语句
        final String sql = boundSql.getSql();
        final Statement stmt;
        if (sql.equals(currentSql) && ms.equals(currentStatement)) {
            //读取statement索引
            int last = statementList.size() - 1;
            //获取最后一个statement
            stmt = statementList.get(last);
            //设置超时时间
            applyTransactionTimeout(stmt);
            handler.parameterize(stmt);//fix Issues 322
            //读取存放结果对象
            BatchResult batchResult = batchResultList.get(last);
            //向结果对象中添加参数
            batchResult.addParameterObject(parameterObject);
        } else {
            //首先读取连接对象
            Connection connection = getConnection(ms.getStatementLog());
            //开始进行预处理
            stmt = handler.prepare(connection, transaction.getTimeout());
            handler.parameterize(stmt);    //fix Issues 322
            //设置sql
            currentSql = sql;
            //设置statement
            currentStatement = ms;
            //将statement加入到列表中
            statementList.add(stmt);
            //向结果集中添加本次结果对象
            batchResultList.add(new BatchResult(ms, sql, parameterObject));
        }
        // handler.parameterize(stmt);
        //开始进行批量处理
        handler.batch(stmt);
        return BATCH_UPDATE_RETURN_VALUE;
    }

    // TODO: 17/4/20 by zmyer
    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameterObject, RowBounds rowBounds,
        ResultHandler resultHandler, BoundSql boundSql)
        throws SQLException {
        Statement stmt = null;
        try {
            //首先刷新statement
            flushStatements();
            //配置对象
            Configuration configuration = ms.getConfiguration();
            //创建statement处理对象
            StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameterObject, rowBounds, resultHandler, boundSql);
            //创建连接对象
            Connection connection = getConnection(ms.getStatementLog());
            //进行sql预处理
            stmt = handler.prepare(connection, transaction.getTimeout());
            //设置statement对象
            handler.parameterize(stmt);
            //开始进行查询操作
            return handler.query(stmt, resultHandler);
        } finally {
            //关闭statement
            closeStatement(stmt);
        }
    }

    // TODO: 17/4/20 by zmyer
    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter,
        RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        flushStatements();
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
        Connection connection = getConnection(ms.getStatementLog());
        Statement stmt = handler.prepare(connection, transaction.getTimeout());
        handler.parameterize(stmt);
        //执行查询操作
        return handler.queryCursor(stmt);
    }

    // TODO: 17/4/20 by zmyer
    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        try {
            List<BatchResult> results = new ArrayList<BatchResult>();
            if (isRollback) {
                return Collections.emptyList();
            }
            for (int i = 0, n = statementList.size(); i < n; i++) {
                Statement stmt = statementList.get(i);
                applyTransactionTimeout(stmt);
                BatchResult batchResult = batchResultList.get(i);
                try {
                    //更新结果集中的记录条数
                    batchResult.setUpdateCounts(stmt.executeBatch());
                    MappedStatement ms = batchResult.getMappedStatement();
                    //读取结果集中的参数列表
                    List<Object> parameterObjects = batchResult.getParameterObjects();
                    KeyGenerator keyGenerator = ms.getKeyGenerator();
                    if (Jdbc3KeyGenerator.class.equals(keyGenerator.getClass())) {
                        Jdbc3KeyGenerator jdbc3KeyGenerator = (Jdbc3KeyGenerator) keyGenerator;
                        //批量处理结果集
                        jdbc3KeyGenerator.processBatch(ms, stmt, parameterObjects);
                    } else if (!NoKeyGenerator.class.equals(keyGenerator.getClass())) { //issue #141
                        for (Object parameter : parameterObjects) {
                            //单独处理
                            keyGenerator.processAfter(this, ms, stmt, parameter);
                        }
                    }
                } catch (BatchUpdateException e) {
                    StringBuilder message = new StringBuilder();
                    message.append(batchResult.getMappedStatement().getId())
                        .append(" (batch index #")
                        .append(i + 1)
                        .append(")")
                        .append(" failed.");
                    if (i > 0) {
                        message.append(" ")
                            .append(i)
                            .append(" prior sub executor(s) completed successfully, but will be rolled back.");
                    }
                    throw new BatchExecutorException(message.toString(), e, results, batchResult);
                }
                //将结果集插入到列表中
                results.add(batchResult);
            }
            return results;
        } finally {
            for (Statement stmt : statementList) {
                closeStatement(stmt);
            }
            currentSql = null;
            statementList.clear();
            batchResultList.clear();
        }
    }
}
