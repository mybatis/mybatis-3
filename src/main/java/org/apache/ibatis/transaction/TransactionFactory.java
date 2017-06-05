/**
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.ibatis.session.TransactionIsolationLevel;

/**
 * Creates {@link Transaction} instances.
 *
 * @author Clinton Begin
 */
// TODO: 17/4/18 by zmyer
public interface TransactionFactory {

    /**
     * Sets transaction factory custom properties.
     *
     * @param props
     */
    // TODO: 17/4/19 by zmyer
    void setProperties(Properties props);

    /**
     * Creates a {@link Transaction} out of an existing connection.
     *
     * @param conn Existing database connection
     * @return Transaction
     * @since 3.1.0
     */
    // TODO: 17/4/19 by zmyer
    Transaction newTransaction(Connection conn);

    /**
     * Creates a {@link Transaction} out of a datasource.
     *
     * @param dataSource DataSource to take the connection from
     * @param level Desired isolation level
     * @param autoCommit Desired autocommit
     * @return Transaction
     * @since 3.1.0
     */
    // TODO: 17/4/19 by zmyer
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level,
        boolean autoCommit);

}
