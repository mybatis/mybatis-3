package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DoubleTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new DoubleTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setDouble(with(any(int.class)), with(any(double.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, 100d, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getDouble(with(any(String.class)));
        will(returnValue(100d));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100d, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getDouble(with(any(int.class)));
        will(returnValue(100d));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100d, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}