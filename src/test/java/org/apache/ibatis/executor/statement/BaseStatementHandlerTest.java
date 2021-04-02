/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.executor.statement;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseStatementHandlerTest {

    @Spy
    Configuration configuration;

    @Mock
    Statement statement;

    private MappedStatement.Builder mappedStatementBuilder;

    @BeforeEach
    void setupMappedStatement() {
        this.mappedStatementBuilder = new MappedStatement.Builder(configuration, "id", new StaticSqlSource(configuration, "sql"), null);
    }

    @AfterEach
    void resetMocks() {
        reset(configuration, statement);
    }

    @Test
    void notSpecifyTimeout() throws SQLException {
        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, null);

        verifyZeroInteractions(statement); // not apply anything
    }

    @Test
    void specifyMappedStatementTimeoutOnly() throws SQLException {
        mappedStatementBuilder.timeout(10);

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, null);

        verify(statement).setQueryTimeout(10); // apply a mapped statement timeout
    }

    @Test
    void specifyDefaultTimeoutOnly() throws SQLException {
        doReturn(20).when(configuration).getDefaultStatementTimeout();

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, null);

        verify(statement).setQueryTimeout(20); // apply a default timeout
    }

    @Test
    void specifyTransactionTimeout() throws SQLException {
        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, 5);

        verify(statement).setQueryTimeout(5); // apply a transaction timeout
    }

    @Test
    void specifyQueryTimeoutZeroAndTransactionTimeout() throws SQLException {
        doReturn(0).when(configuration).getDefaultStatementTimeout();

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, 5);

        verify(statement).setQueryTimeout(5); // apply a transaction timeout
    }

    @Test
    void specifyMappedStatementTimeoutAndDefaultTimeout() throws SQLException {
        doReturn(20).when(configuration).getDefaultStatementTimeout();
        mappedStatementBuilder.timeout(30);

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, null);

        verify(statement).setQueryTimeout(30); // apply a mapped statement timeout
        verify(configuration, never()).getDefaultStatementTimeout();
    }

    @Test
    void specifyQueryTimeoutAndTransactionTimeoutMinIsQueryTimeout() throws SQLException {
        doReturn(10).when(configuration).getDefaultStatementTimeout();

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, 20);

        verify(statement).setQueryTimeout(10); // apply a query timeout
    }

    @Test
    void specifyQueryTimeoutAndTransactionTimeoutMinIsTransactionTimeout() throws SQLException {
        doReturn(10).when(configuration).getDefaultStatementTimeout();

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, 5);

        verify(statement).setQueryTimeout(10);
        verify(statement).setQueryTimeout(5); // apply a transaction timeout
    }

    @Test
    void specifyQueryTimeoutAndTransactionTimeoutWithSameValue() throws SQLException {
        doReturn(10).when(configuration).getDefaultStatementTimeout();

        BaseStatementHandler handler = new SimpleStatementHandler(null, mappedStatementBuilder.build(), null, null, null, null);
        handler.setStatementTimeout(statement, 10);

        verify(statement).setQueryTimeout(10);
    }

}
