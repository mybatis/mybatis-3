package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FloatTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new FloatTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setFloat(with(any(int.class)), with(any(float.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, 100f, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getFloat(with(any(String.class)));
        will(returnValue(100f));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100f, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getFloat(with(any(int.class)));
        will(returnValue(100f));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100f, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}