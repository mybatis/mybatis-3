/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.custom_method;

import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * Custom implementation class which implements a custom method.
 * @author Kazuki Shimizu
 */
public class PersonCollector {

    private static final String MAPPER_NAME = PersonMapper.class.getName();

    public long collect(SqlSession sqlSession, RowBounds rowBounds, ResultHandler resultHandler) {
        Long count = sqlSession.selectOne(MAPPER_NAME + ".countAll");
        if (0 < count) {
            sqlSession.select(MAPPER_NAME + ".collect", null, rowBounds, resultHandler);
        }
        return count;
    }

}
