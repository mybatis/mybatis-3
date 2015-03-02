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
package org.apache.ibatis.binding;

import org.apache.ibatis.BaseDataTest;
import domain.blog.Author;
import domain.blog.Post;
import domain.blog.Section;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class FlushTest {
    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setup() throws Exception {
        DataSource dataSource = BaseDataTest.createBlogDataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setDefaultExecutorType(ExecutorType.BATCH);
        configuration.getTypeAliasRegistry().registerAlias(Post.class);
        configuration.getTypeAliasRegistry().registerAlias(Author.class);
        configuration.addMapper(BoundAuthorMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    public void invokeFlushStatementsViaMapper() {

        SqlSession session = sqlSessionFactory.openSession();

        try {

            BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
            Author author = new Author(-1, "cbegin", "******", "cbegin@nowhere.com", "N/A", Section.NEWS);
            List<Integer> ids = new ArrayList<Integer>();
            mapper.insertAuthor(author);
            ids.add(author.getId());
            mapper.insertAuthor(author);
            ids.add(author.getId());
            mapper.insertAuthor(author);
            ids.add(author.getId());
            mapper.insertAuthor(author);
            ids.add(author.getId());
            mapper.insertAuthor(author);
            ids.add(author.getId());

            // test
            List<BatchResult> results = mapper.flush();

            assertThat(results.size(), is(1));
            assertThat(results.get(0).getUpdateCounts().length, is(ids.size()));

            for (int id : ids) {
                Author selectedAuthor = mapper.selectAuthor(id);
                assertNotNull(id + " is not found.", selectedAuthor);
            }

            session.rollback();
        } finally {
            session.close();
        }

    }

}
