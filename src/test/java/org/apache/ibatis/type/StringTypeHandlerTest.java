package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StringTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new StringTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setString(with(any(int.class)), with(any(String.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldSetNullParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setNull(with(any(int.class)), with(any(int.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.VARCHAR);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getString(with(any(String.class)));
        will(returnValue("Hello"));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetNullResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getString(with(any(String.class)));
        will(returnValue(null));
        one(rs).wasNull();
        will(returnValue(true));
      }
    });
    assertEquals(null, TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getString(with(any(int.class)));
        will(returnValue("Hello"));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetNullResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getString(with(any(int.class)));
        will(returnValue(null));
        one(cs).wasNull();
        will(returnValue(true));
      }
    });
    assertEquals(null, TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}
