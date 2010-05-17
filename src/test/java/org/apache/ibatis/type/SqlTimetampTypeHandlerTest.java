package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Date;

public class SqlTimetampTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new SqlTimestampTypeHandler();
  private static final java.sql.Timestamp SQL_TIME = new java.sql.Timestamp(new Date().getTime());

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setTimestamp(with(any(int.class)), with(any(java.sql.Timestamp.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, SQL_TIME, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getTimestamp(with(any(String.class)));
        will(returnValue(SQL_TIME));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(SQL_TIME, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getTimestamp(with(any(int.class)));
        will(returnValue(SQL_TIME));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(SQL_TIME, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}