package org.apache.ibatis.submitted.xml_references;

import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

import com.ibatis.common.resources.Resources;

public class EnumWithOgnlTest {
    
    @Test
    public void testConfiguration() {
        UnpooledDataSourceFactory dataSourceFactory = new UnpooledDataSourceFactory();
        Properties dataSourceProperties = new Properties();
        dataSourceProperties.put("driver", "org.hsqldb.jdbcDriver");
        dataSourceProperties.put("url", "jdbc:hsqldb:mem:xml_references");
        dataSourceProperties.put("username", "sa");
        dataSourceFactory.setProperties(dataSourceProperties);
        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSourceFactory.getDataSource());
        Configuration configuration = new Configuration();
        configuration.setEnvironment(environment);
        configuration.getTypeAliasRegistry().registerAlias(Person.class);
        configuration.addMapper(PersonMapper.class);
        configuration.addMapper(PersonMapper2.class);
        new DefaultSqlSessionFactory(configuration);
    }
    @Test
    public void testMixedConfiguration() throws Exception {
    	Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/xml_references/ibatisConfig.xml");
    	SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    	sqlSessionFactory.getConfiguration().addMapper(PersonMapper2.class);
    }
}
