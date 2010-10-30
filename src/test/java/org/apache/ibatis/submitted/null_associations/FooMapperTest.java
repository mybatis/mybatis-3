package org.apache.ibatis.submitted.null_associations;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.*;

import java.io.Reader;
import java.sql.Connection;

public class FooMapperTest {

  private final static String SQL_MAP_CONFIG = "org/apache/ibatis/submitted/null_associations/sqlmap.xml";
  private static SqlSession session;

  @BeforeClass
  public static void setUpBeforeClass() {
    try {
      final SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(SQL_MAP_CONFIG));
      session = factory.openSession();
      Connection conn = session.getConnection();
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/null_associations/create-schema-mysql.sql");
      runner.runScript(reader);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Before
  public void setUp() {
    final FooMapper mapper = session.getMapper(FooMapper.class);
    mapper.deleteAllFoo();
    session.commit();
  }

  @Test
  public void testNullAssociation() {
    final FooMapper mapper = session.getMapper(FooMapper.class);
    final Foo foo = new Foo(1L, null, true);
    mapper.insertFoo(foo);
    session.commit();
    final Foo read = mapper.selectFoo();
    Assert.assertEquals("Invalid mapping", 1L, read.getField1());
    Assert.assertNull("Invalid mapping - field2 (Bar) should be null", read.getField2());
    Assert.assertTrue("Invalid mapping", read.isField3());
  }

  @Test
  public void testNotNullAssociation() {
    final FooMapper mapper = session.getMapper(FooMapper.class);
    final Bar bar = new Bar(1L, 2L, 3L);
    final Foo foo = new Foo(1L, bar, true);
    mapper.insertFoo(foo);
    session.commit();
    final Foo read = mapper.selectFoo();
    Assert.assertEquals("Invalid mapping", 1L, read.getField1());
    Assert.assertNotNull("Bar should be not null", read.getField2());
    Assert.assertTrue("Invalid mapping", read.isField3());
    Assert.assertEquals("Invalid mapping", 1L, read.getField2().getField1());
    Assert.assertEquals("Invalid mapping", 2L, read.getField2().getField2());
    Assert.assertEquals("Invalid mapping", 3L, read.getField2().getField3());
  }

  @AfterClass
  public static void tearDownAfterClass() {
    session.close();
  }

}
