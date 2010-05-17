package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.Reader;
import java.sql.Clob;

public class ClobTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new ClobTypeHandler();

  protected final Clob clob = mockery.mock(Clob.class);

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setCharacterStream(with(any(int.class)), with(any(Reader.class)), with(any(int.class)));
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
        one(rs).getClob(with(any(String.class)));
        will(returnValue(clob));
        one(rs).wasNull();
        will(returnValue(false));
        one(clob).length();
        will(returnValue(3l));
        one(clob).getSubString(with(any(long.class)), with(any(int.class)));
        will(returnValue("Hello"));
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
        one(cs).getClob(with(any(int.class)));
        will(returnValue(clob));
        one(cs).wasNull();
        will(returnValue(false));
        one(clob).length();
        will(returnValue(3l));
        one(clob).getSubString(with(any(long.class)), with(any(int.class)));
        will(returnValue("Hello"));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}