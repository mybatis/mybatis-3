package org.apache.ibatis.builder.test;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class XMLMapperBuilderTest extends BaseDataTest {

    @Test
    public void testParse(){
        boolean noExceptionThrow = true ;
        try {
            String resource = "org/apache/ibatis/builder/test/MapperConfig.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            new SqlSessionFactoryBuilder().build(inputStream);
        }catch (Exception exp){
            assertTrue( exp.getMessage().contains("MappedStatementNotFoundException"));
            noExceptionThrow = false ;
        }
        if (noExceptionThrow){
            assertTrue(false);
        }

    }
}
