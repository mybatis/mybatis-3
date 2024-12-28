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
package org.apache.ibatis.transaction.jdbc;

import java.sql.SQLException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see JdbcTransaction
 */
@ExtendWith(MockitoExtension.class)
abstract class JdbcTransactionBase {

  abstract void shouldGetConnection() throws SQLException;

  abstract void shouldCommitWhenConnectionIsNotAutoCommit() throws SQLException;

  abstract void shouldAutoCommitWhenConnectionIsAutoCommit() throws SQLException;

  abstract void shouldRollbackWhenConnectionIsNotAutoCommit() throws SQLException;

  abstract void shouldAutoRollbackWhenConnectionIsAutoCommit() throws SQLException;

  abstract void shouldCloseAndSetAutoCommitWhenConnectionIsNotAutoCommit() throws SQLException;

  abstract void shouldCloseAndNotSetAutoCommitWhenConnectionIsAutoCommit() throws SQLException;

  abstract void shouldReturnNullWhenGetTimeout() throws SQLException;
}
