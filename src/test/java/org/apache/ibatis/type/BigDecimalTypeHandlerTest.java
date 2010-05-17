package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.math.BigDecimal;

public class BigDecimalTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new BigDecimalTypeHandler();

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setBigDecimal(with(any(int.class)), with(any(BigDecimal.class)));
      }
    });
    TYPE_HANDLER.setParameter(ps, 1, new BigDecimal(1), null);
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromResultSet()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(rs).getBigDecimal(with(any(String.class)));
        will(returnValue(new BigDecimal(1)));
        one(rs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(new BigDecimal(1), TYPE_HANDLER.getResult(rs, "column"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void shouldGetResultFromCallableStatement()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(cs).getBigDecimal(with(any(int.class)));
        will(returnValue(new BigDecimal(1)));
        one(cs).wasNull();
        will(returnValue(false));
      }
    });
    assertEquals(new BigDecimal(1), TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}
