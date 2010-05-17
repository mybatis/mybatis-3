package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ByteTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new ByteTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setByte(with(any(int.class)), with(any(byte.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, (byte) 100, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getByte(with(any(String.class)));
        will(returnValue((byte) 100));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals((byte) 100, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getByte(with(any(int.class)));
        will(returnValue((byte) 100));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals((byte) 100, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}