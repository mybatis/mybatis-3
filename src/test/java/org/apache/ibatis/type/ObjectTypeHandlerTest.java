package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ObjectTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new ObjectTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setObject(with(any(int.class)), with(any(String.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getObject(with(any(String.class)));
        will(returnValue("Hello"));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getObject(with(any(int.class)));
        will(returnValue("Hello"));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}