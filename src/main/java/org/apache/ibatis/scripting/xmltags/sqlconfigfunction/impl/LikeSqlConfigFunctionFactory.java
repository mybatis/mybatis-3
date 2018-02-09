/**
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.scripting.xmltags.sqlconfigfunction.impl;

import java.util.Arrays;
import java.util.Collection;

import org.apache.ibatis.scripting.xmltags.sqlconfigfunction.SqlConfigFunction;
import org.apache.ibatis.scripting.xmltags.sqlconfigfunction.SqlConfigFunctionFactory;

/**
 * @author Felix Lin
 * @since 3.4.6
 */
/* package */ class LikeSqlConfigFunctionFactory implements SqlConfigFunctionFactory {

    @Override
    public Collection<SqlConfigFunction> getSqlConfigFunctions() {
        return Arrays.asList(getLeftLikeSqlConfigFunction(), getRightLikeSqlConfigFunction(), getLikeSqlConfigFunction());
    }

    private SqlConfigFunction getLeftLikeSqlConfigFunction() {
        return new AbstractLikeSqlConfigFunction() {
            @Override
            public String getName() {
                return "llike";
            }

            @Override
            protected String eval(String arg) {
                return "LIKE $concat{'%'," + arg + "}";
            }
        };
    }

    private SqlConfigFunction getRightLikeSqlConfigFunction() {
        return new AbstractLikeSqlConfigFunction() {
            @Override
            public String getName() {
                return "rlike";
            }

            @Override
            protected String eval(String arg) {
                return "LIKE $concat{" + arg + ", '%'}";
            }
        };
    }

    private SqlConfigFunction getLikeSqlConfigFunction() {
        return new AbstractLikeSqlConfigFunction() {
            @Override
            public String getName() {
                return "like";
            }

            @Override
            protected String eval(String arg) {
                return "LIKE $concat{'%'," + arg + ", '%'}";
            }
        };
    }

    private abstract class AbstractLikeSqlConfigFunction extends AbstractDatabaseIdSqlConfigFunction {
        @Override
        protected String eval(String databaseId, String[] args) {
            assertEqualArgsCount(args, 1);
            return eval(args[0]);
        }

        protected abstract String eval(String arg);
    }
}
