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
package org.apache.ibatis.mapping;

import javax.sql.DataSource;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * @author Clinton Begin
 */
// TODO: 17/4/18 by zmyer
public final class Environment {
    //对象id
    private final String id;
    //事务工厂
    private final TransactionFactory transactionFactory;
    //数据源
    private final DataSource dataSource;

    // TODO: 17/4/19 by zmyer
    public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        if (transactionFactory == null) {
            throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
        }
        this.id = id;
        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }

    // TODO: 17/4/19 by zmyer
    public static class Builder {
        //对象id
        private String id;
        //事务工厂
        private TransactionFactory transactionFactory;
        //数据源
        private DataSource dataSource;

        // TODO: 17/4/19 by zmyer
        public Builder(String id) {
            this.id = id;
        }

        // TODO: 17/4/19 by zmyer
        public Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        // TODO: 17/4/19 by zmyer
        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        // TODO: 17/4/19 by zmyer
        public String id() {
            return this.id;
        }

        // TODO: 17/4/19 by zmyer
        public Environment build() {
            return new Environment(this.id, this.transactionFactory, this.dataSource);
        }

    }

    // TODO: 17/4/19 by zmyer
    public String getId() {
        return this.id;
    }

    // TODO: 17/4/19 by zmyer
    public TransactionFactory getTransactionFactory() {
        return this.transactionFactory;
    }

    // TODO: 17/4/19 by zmyer
    public DataSource getDataSource() {
        return this.dataSource;
    }

}
