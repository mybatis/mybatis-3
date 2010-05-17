package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ShortTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new ShortTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setShort(with(any(int.class)), with(any(short.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, (short) 100, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getShort(with(any(String.class)));
        will(returnValue((short) 100));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals((short) 100, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getShort(with(any(int.class)));
        will(returnValue((short) 100));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals((short) 100, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}