package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public class ByteArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new ByteArrayTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setBytes(with(any(int.class)), with(any(byte[].class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, new byte[]{1, 2, 3}, null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getBytes(with(any(String.class)));
        will(returnValue(new byte[]{1, 2, 3}));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getBytes(with(any(int.class)));
        will(returnValue(new byte[]{1, 2, 3}));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}