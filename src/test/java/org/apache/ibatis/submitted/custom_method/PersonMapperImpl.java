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

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation class which implements a custom method.
 * @author Kazuki Shimizu
 */
public class PersonMapperImpl {

    private static final String MAPPER_NAME = PersonMapper.class.getName();

    public Page<Person> findPage(SqlSession sqlSession) {
        return findPage(sqlSession, new RowBounds());
    }

    public Page<Person> findPage(SqlSession sqlSession, RowBounds rowBounds) {
        Long count = sqlSession.selectOne(MAPPER_NAME + ".countAll");
        List<Person> content;
        if (0 < count) {
            content = sqlSession.selectList(MAPPER_NAME + ".findList", null, rowBounds);
        } else {
            content = Collections.emptyList();
        }
        return new Page<Person>(content, count, rowBounds);
    }

    public Page<Person> findPageByName(SqlSession sqlSession, String name, RowBounds rowBounds) {
        Long count = sqlSession.selectOne(MAPPER_NAME + ".countByName", name);
        List<Person> content;
        if (0 < count) {
            content = sqlSession.selectList(MAPPER_NAME + ".findListByName", name, rowBounds);
        } else {
            content = Collections.emptyList();
        }
        return new Page<Person>(content, count, rowBounds);
    }


    public Page<Person> findPageDelegatingMapperInterface(SqlSession sqlSession, RowBounds rowBounds) {
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        Long count = personMapper.countAll();
        List<Person> content;
        if (0 < count) {
            content = personMapper.findList(rowBounds);
        } else {
            content = Collections.emptyList();
        }
        return new Page<Person>(content, count, rowBounds);
    }

    public long findPageThrowRuntimeException(SqlSession sqlSession) {
        throw new PersistenceException("Test for handling a runtime exception.", new SQLException());
    }

    public long findPageThrowError(SqlSession sqlSession) {
        throw new OutOfMemoryError("Test for handling an error.");
    }

    // Not access from other class
    private long findPageThrowException(SqlSession sqlSession) {
        return 0;
    }

}
