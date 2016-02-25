package org.apache.ibatis.session;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Kazuki Shimizu
 * @since 3.3.2
 */
public class AutoMappingUnknownColumnBehaviorTest {

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

    public static class LastEventSavedAppender extends NullAppender {
        private static LoggingEvent event;

        public void doAppend(LoggingEvent event) {
            LastEventSavedAppender.event = event;
        }
    }

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setup() throws Exception {
        DataSource dataSource = BaseDataTest.createBlogDataSource();
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(Mapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    public void none() {
        sqlSessionFactory.getConfiguration().setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.NONE);
        SqlSession session = sqlSessionFactory.openSession();
        try {
            Mapper mapper = session.getMapper(Mapper.class);
            Author author = mapper.selectAuthor(101);
            assertThat(author.getId(), is(101));
            assertThat(author.getUsername(), nullValue());
        } finally {
            session.close();
        }

    }

    @Test
    public void warningCauseByUnknownPropertyType() {
        sqlSessionFactory.getConfiguration().setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.WARNING);

        SqlSession session = sqlSessionFactory.openSession();

        try {
            Mapper mapper = session.getMapper(Mapper.class);
            SimpleAuthor author = mapper.selectSimpleAuthor(101);
            assertThat(author.getId(), nullValue());
            assertThat(author.getUsername(), is("jim"));
            assertThat(LastEventSavedAppender.event.getMessage().toString(), is("Unknown column is detected on auto-mapping. Mapping parameters are [columnName=ID,propertyName=id,propertyType=java.util.concurrent.atomic.AtomicInteger]"));

        } finally {
            session.close();
        }

    }

    @Test
    public void failingCauseByUnknownColumn() {
        sqlSessionFactory.getConfiguration().setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.FAILING);

        SqlSession session = sqlSessionFactory.openSession();

        try {
            Mapper mapper = session.getMapper(Mapper.class);
            mapper.selectAuthor(101);
        } catch (PersistenceException e) {
            assertThat(e.getCause(), instanceOf(SqlSessionException.class));
            assertThat(e.getCause().getMessage(), is("Unknown column is detected on auto-mapping. Mapping parameters are [columnName=USERNAMEEEE,propertyName=USERNAMEEEE,propertyType=null]"));
        } finally {
            session.close();
        }

    }

}
