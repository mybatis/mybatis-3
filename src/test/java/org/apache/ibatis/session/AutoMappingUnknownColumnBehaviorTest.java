/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for specify the behavior when detects an unknown column (or unknown property type) of automatic mapping target.
 *
 * @since 3.4.0
 * @author Kazuki Shimizu
 */
class AutoMappingUnknownColumnBehaviorTest {

    interface Mapper {
        @Select({
                "SELECT ",
                "  ID,",
                "  USERNAME as USERNAMEEEE,", // unknown column
                "  PASSWORD,",
                "  EMAIL,",
                "  BIO",
                "FROM AUTHOR WHERE ID = #{id}"})
        Author selectAuthor(int id);

        @Select({
                "SELECT ",
                "  ID,", // unknown property type
                "  USERNAME",
                "FROM AUTHOR WHERE ID = #{id}"})
        SimpleAuthor selectSimpleAuthor(int id);
    }

    static class SimpleAuthor {
        private AtomicInteger id; // unknown property type
        private String username;

        public AtomicInteger getId() {
            return id;
        }

        public void setId(AtomicInteger id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    public static class LastEventSavedAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
        private static ILoggingEvent lastEvent;

        @Override
        protected void append(ILoggingEvent event) {
          lastEvent = event;
        }
    }

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setup() throws Exception {
        DataSource dataSource = BaseDataTest.createBlogDataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(Mapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void none() {
        sqlSessionFactory.getConfiguration().setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.NONE);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Mapper mapper = session.getMapper(Mapper.class);
            Author author = mapper.selectAuthor(101);
            assertThat(author.getId()).isEqualTo(101);
            assertThat(author.getUsername()).isNull();
        }
    }

    @Test
    void warningCauseByUnknownPropertyType() {
        sqlSessionFactory.getConfiguration().setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.WARNING);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Mapper mapper = session.getMapper(Mapper.class);
            SimpleAuthor author = mapper.selectSimpleAuthor(101);
            assertThat(author.getId()).isNull();
            assertThat(author.getUsername()).isEqualTo("jim");
            assertThat(LastEventSavedAppender.lastEvent.getMessage()).isEqualTo("Unknown column is detected on 'org.apache.ibatis.session.AutoMappingUnknownColumnBehaviorTest$Mapper.selectSimpleAuthor' auto-mapping. Mapping parameters are [columnName=ID,propertyName=id,propertyType=java.util.concurrent.atomic.AtomicInteger]");
        }
    }

    @Test
    void failingCauseByUnknownColumn() {
        sqlSessionFactory.getConfiguration().setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.FAILING);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Mapper mapper = session.getMapper(Mapper.class);
            mapper.selectAuthor(101);
        } catch (PersistenceException e) {
            assertThat(e.getCause()).isInstanceOf(SqlSessionException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Unknown column is detected on 'org.apache.ibatis.session.AutoMappingUnknownColumnBehaviorTest$Mapper.selectAuthor' auto-mapping. Mapping parameters are [columnName=USERNAMEEEE,propertyName=USERNAMEEEE,propertyType=null]");
        }
    }

}
