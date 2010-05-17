package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.Date;

public class DateOnlyTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new DateOnlyTypeHandler();
  private static final Date DATE = new Date();
  private static final java.sql.Date SQL_DATE = new java.sql.Date(DATE.getTime());

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setDate(with(any(int.class)), with(any(java.sql.Date.class)));
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
        one(rs).getDate(with(any(String.class)));
        will(returnValue(SQL_DATE));
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
        one(cs).getDate(with(any(int.class)));
        will(returnValue(SQL_DATE));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(DATE, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}