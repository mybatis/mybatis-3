package org.apache.ibatis.binding.issue35;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.List;

/**
 * if run with mybatis-3.3.0 this generates BindingException with statement not found
 */
public class NameEntityMapperTest {

    private static final String DS_PROPERTIES = "org/apache/ibatis/binding/issue35/ds.properties";
    private static final String DS_DDL = "org/apache/ibatis/binding/issue35/ddl.sql";

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setup() throws Exception {
        DataSource dataSource = BaseDataTest.createUnpooledDataSource(DS_PROPERTIES);
        BaseDataTest.runScript(dataSource, DS_DDL);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("Production", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(GeneratedNameEntityMapper.class);
        configuration.addMapper(CustomNameEntityMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    public void testStatementFound() throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            CustomNameEntityMapper nameEntityMapper = sqlSession.getMapper(CustomNameEntityMapper.class);
            nameEntityMapper.insert(new NameEntity(1, "Test 1", 25));
            nameEntityMapper.insert(new NameEntity(2, "Test 2", 35));
            nameEntityMapper.insert(new NameEntity(3, "Test 3", 45));
            List<NameEntity> olderThan = nameEntityMapper.selectOlderThan(25);
            Assert.assertEquals(2, olderThan.size());
        }
        finally {
            sqlSession.close();
        }
    }
}
