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
package org.apache.ibatis.submitted.lazy_immutable;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;

import java.io.Reader;

import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ImmutablePOJOTest {

    private static final Integer POJO_ID = 1;
    private static SqlSessionFactory factory;

    @BeforeAll
    static void setupClass() throws Exception {
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/lazy_immutable/ibatisConfig.xml")) {
            factory = new SqlSessionFactoryBuilder().build(reader);
        }

        BaseDataTest.runScript(factory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/lazy_immutable/CreateDB.sql");
    }

    @Test
    void testLoadLazyImmutablePOJO() {
        try (SqlSession session = factory.openSession()) {
            final ImmutablePOJOMapper mapper = session.getMapper(ImmutablePOJOMapper.class);
            final ImmutablePOJO pojo = mapper.getImmutablePOJO(POJO_ID);

            assertEquals(POJO_ID, pojo.getId());
            assertNotNull(pojo.getDescription(), "Description should not be null.");
            assertFalse(pojo.getDescription().length() == 0, "Description should not be empty.");
        }
    }

}
