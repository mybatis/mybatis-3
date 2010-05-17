package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

public class DateTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new DateTypeHandler();
  private static final Date DATE = new Date();
  private static final Timestamp TIMESTAMP = new Timestamp(DATE.getTime());

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setTimestamp(with(any(int.class)), with(any(Timestamp.class)));
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
        one(rs).getTimestamp(with(any(String.class)));
        will(returnValue(TIMESTAMP));
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
        one(cs).getTimestamp(with(any(int.class)));
        will(returnValue(TIMESTAMP));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(DATE, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}