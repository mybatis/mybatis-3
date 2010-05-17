package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BooleanTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new BooleanTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setBoolean(with(any(int.class)), with(any(boolean.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, true, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getBoolean(with(any(String.class)));
        will(returnValue(true));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(true, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getBoolean(with(any(int.class)));
        will(returnValue(true));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(true, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}