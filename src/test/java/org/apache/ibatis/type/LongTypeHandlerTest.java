package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LongTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new LongTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setLong(with(any(int.class)), with(any(long.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, 100l, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getLong(with(any(String.class)));
        will(returnValue(100l));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100l, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getLong(with(any(int.class)));
        will(returnValue(100l));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100l, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}