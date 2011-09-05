package org.apache.ibatis.submitted.awful_table;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class AwfulTableTest {
    protected SqlSessionFactory sqlSessionFactory;

    @Before
    public void setUp() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:attest",
                "sa", "");

        Reader reader = Resources
                .getResourceAsReader("org/apache/ibatis/submitted/awful_table/CreateDB.sql");

        ScriptRunner runner = new ScriptRunner(conn);
        runner.setLogWriter(null);
        runner.setErrorLogWriter(null);
        runner.runScript(reader);
        conn.commit();
        reader.close();

        reader = Resources
                .getResourceAsReader("org/apache/ibatis/submitted/awful_table/MapperConfig.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();
    }

    @Test
    public void testAwfulTableInsert() {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            AwfulTableMapper mapper = sqlSession
                    .getMapper(AwfulTableMapper.class);
            AwfulTable record = new AwfulTable();
            record.seteMail("fred@fred.com");
            record.setEmailaddress("alsofred@fred.com");
            record.setFirstFirstName("fred1");
            record.setFrom("from field");
            record.setId1(1);
            record.setId2(2);
            record.setId5(5);
            record.setId6(6);
            record.setId7(7);
            record.setSecondFirstName("fred2");
            record.setThirdFirstName("fred3");

            mapper.insert(record);
            Integer generatedCustomerId = record.getCustomerId();
            assertEquals(57, generatedCustomerId.intValue());

            AwfulTable returnedRecord = mapper
                    .selectByPrimaryKey(generatedCustomerId);

            assertEquals(generatedCustomerId, returnedRecord.getCustomerId());
            assertEquals(record.geteMail(), returnedRecord.geteMail());
            assertEquals(record.getEmailaddress(),
                    returnedRecord.getEmailaddress());
            assertEquals(record.getFirstFirstName(),
                    returnedRecord.getFirstFirstName());
            assertEquals(record.getFrom(), returnedRecord.getFrom());
            assertEquals(record.getId1(), returnedRecord.getId1());
            assertEquals(record.getId2(), returnedRecord.getId2());
            assertEquals(record.getId5(), returnedRecord.getId5());
            assertEquals(record.getId6(), returnedRecord.getId6());
            assertEquals(record.getId7(), returnedRecord.getId7());
            assertEquals(record.getSecondFirstName(),
                    returnedRecord.getSecondFirstName());
            assertEquals(record.getThirdFirstName(),
                    returnedRecord.getThirdFirstName());
        } finally {
            sqlSession.close();
        }
    }

}
