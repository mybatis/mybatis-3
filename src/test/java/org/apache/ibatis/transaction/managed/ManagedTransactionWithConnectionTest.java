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
package org.apache.ibatis.transaction.managed;

import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @see ManagedTransaction
 */
class ManagedTransactionWithConnectionTest extends TransactionTest {

	@Mock
	private Connection connection;

	private Transaction transaction;

	@BeforeEach
	void setup() {
		this.transaction = new ManagedTransaction(connection, true);
	}

	@Test
	@Override
	public void shouldGetConnection() throws SQLException {
		Connection result = transaction.getConnection();

		assertEquals(connection, result);
	}

	@Test
	@Override
	public void shouldCommit() throws SQLException {
		transaction.commit();

		verify(connection, never()).commit();
	}

	@Test
	@Override
	public void shouldRollback() throws SQLException {
		transaction.commit();

		verify(connection, never()).rollback();
	}

	@Test
	@Override
	public void shouldClose() throws SQLException {
		transaction.close();

		verify(connection).close();
	}

	@Test
	void shouldNotCloseWhenSetNonCloseConnection() throws SQLException {
		this.transaction = new ManagedTransaction(connection, false);

		transaction.close();

		verify(connection, never()).close();
	}

	@Test
	@Override
	public void shouldGetTimeout() throws SQLException {
		assertNull(transaction.getTimeout());
	}

}
