package org.apache.ibatis.metadata;

import org.apache.ibatis.BaseDataTest;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Types;

public class MetadataTest extends BaseDataTest {

  private DataSource dataSource;

  @Before
  public void setup() throws Exception {
    dataSource = createUnpooledDataSource(BaseDataTest.BLOG_PROPERTIES);
  }

  @Test
  public void testShouldGetAllTableNames() throws Exception {
    Connection connection = dataSource.getConnection();
    try {
      Database db = DatabaseFactory.newDatabase(connection, null, "APP");
      assertNotNull(db.getTable("blog"));
      assertNotNull(db.getTable("Author"));
      assertNotNull(db.getTable("tAg"));
      assertNotNull(db.getTable("PosT"));
    } finally {
      connection.close();
    }
  }

  @Test
  public void testShouldEnsureDatabasesTablesAndColumnsEqualityWorks() throws Exception {
    Connection connection = dataSource.getConnection();
    try {
      Database db = DatabaseFactory.newDatabase(connection, null, null);
      Database db2 = DatabaseFactory.newDatabase(connection, null, null);
      assertNotNull(db);
      assertTrue(db.equals(db2));
      assertTrue(db.hashCode() == db.hashCode());
      Table t = db.getTable("blog");
      Table t2 = db.getTable("author");
      assertNotNull(t);
      assertNotNull(t2);
      assertFalse(t.equals(t2));
      assertTrue(t.getCatalog().equals(t.getCatalog()));
      assertTrue(t.getSchema().equals(t.getSchema()));
      assertTrue(t.equals(t));
      assertTrue(t.hashCode() == t.hashCode());
      assertEquals("BLOG", t.getName());
      assertEquals(3, t.getColumnNames().length);
      Column c = t.getColumn("author_id");
      Column c2 = t.getColumn("id");
      assertNotNull(c);
      assertNotNull(c2);
      assertEquals(Types.INTEGER, c.getType());
      assertFalse(c.equals(c2));
      assertTrue(c.equals(c));
      assertTrue(c.hashCode() == c.hashCode());
    } finally {
      connection.close();
    }
  }

}
