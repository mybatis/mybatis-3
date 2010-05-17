package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Date;

public class TimeOnlyTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new TimeOnlyTypeHandler();
  private static final Date DATE = new Date();
  private static final java.sql.Time SQL_TIME = new java.sql.Time(DATE.getTime());

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setTime(with(any(int.class)), with(any(java.sql.Time.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, DATE, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getTime(with(any(String.class)));
        will(returnValue(SQL_TIME));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(DATE, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getTime(with(any(int.class)));
        will(returnValue(SQL_TIME));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(DATE, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}