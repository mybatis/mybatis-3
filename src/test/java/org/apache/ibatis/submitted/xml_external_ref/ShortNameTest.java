package org.apache.ibatis.submitted.xml_external_ref;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

public class ShortNameTest {
    @Test
    public void getStatementByShortName() throws Exception {
        Configuration configuration = getConfiguration();
        // statement can be referenced by its short name.
        MappedStatement selectPet = configuration.getMappedStatement("selectPet");
        assertNotNull(selectPet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ambiguousShortNameShouldFail() throws Exception {
        Configuration configuration = getConfiguration();
        // ambiguous short name should throw an exception.
        MappedStatement ambiguousStatement = configuration.getMappedStatement("select");
        fail("If there are multiple statements with the same name, an exception should be thrown.");
    }

    private Configuration getConfiguration() throws IOException {
        Reader configReader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/MapperConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
        configReader.close();
        return sqlSessionFactory.getConfiguration();
    }
}
