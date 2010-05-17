package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class IntegerTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new IntegerTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setInt(with(any(int.class)), with(any(int.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, 100, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getInt(with(any(String.class)));
        will(returnValue(100));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getInt(with(any(int.class)));
        will(returnValue(100));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(100, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}