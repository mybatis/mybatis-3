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

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

/**
 * @author Clinton Begin
 */
// TODO: 17/4/18 by zmyer
public class DefaultSqlSessionFactory implements SqlSessionFactory {
    //配置对象
    private final Configuration configuration;

    // TODO: 17/4/18 by zmyer
    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(boolean autoCommit) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(ExecutorType execType) {
        return openSessionFromDataSource(execType, null, false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return openSessionFromDataSource(execType, level, false);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return openSessionFromDataSource(execType, null, autoCommit);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(Connection connection) {
        return openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection) {
        return openSessionFromConnection(execType, connection);
    }

    // TODO: 17/4/18 by zmyer
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    // TODO: 17/4/18 by zmyer
    private SqlSession openSessionFromDataSource(ExecutorType execType,
        TransactionIsolationLevel level, boolean autoCommit) {
        //事务对象
        Transaction tx = null;
        try {
            //环境变量对象
            final Environment environment = configuration.getEnvironment();
            //创建事务工厂对象
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            //创建事务对象
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
            //根据事务对象以及执行器类型,创建执行器对象
            final Executor executor = configuration.newExecutor(tx, execType);
            //创建sql会话对象
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            closeTransaction(tx); // may have fetched a connection so lets call close()
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
        try {
            boolean autoCommit;
            try {
                //是否自动提交事务
                autoCommit = connection.getAutoCommit();
            } catch (SQLException e) {
                // Failover to true, as most poor drivers
                // or databases won't support transactions
                autoCommit = true;
            }
            //创建环境变量对象
            final Environment environment = configuration.getEnvironment();
            //创建事务工厂
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            //创建事务对象
            final Transaction tx = transactionFactory.newTransaction(connection);
            //根据事务对象创建执行器
            final Executor executor = configuration.newExecutor(tx, execType);
            //创建sql会话对象
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    // TODO: 17/4/18 by zmyer
    private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
        if (environment == null || environment.getTransactionFactory() == null) {
            return new ManagedTransactionFactory();
        }
        return environment.getTransactionFactory();
    }

    // TODO: 17/4/18 by zmyer
    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException ignore) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }
}
