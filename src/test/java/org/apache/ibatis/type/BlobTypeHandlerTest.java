package org.apache.ibatis.type;

import org.jmock.Expectations;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

import java.io.InputStream;
import java.sql.Blob;

public class BlobTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler TYPE_HANDLER = new BlobTypeHandler();

  protected final Blob blob = mockery.mock(Blob.class);

  @Test
  public void shouldSetParameter()
      throws Exception {
    mockery.checking(new Expectations() {
      {
        one(ps).setBinaryStream(with(any(int.class)), with(any(InputStream.class)), with(any(int.class)));
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
        one(rs).getBlob(with(any(String.class)));
        will(returnValue(blob));
        one(rs).wasNull();
        will(returnValue(false));
        one(blob).length();
        will(returnValue(3l));
        one(blob).getBytes(with(any(long.class)), with(any(int.class)));
        will(returnValue(new byte[]{1, 2, 3}));
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
        one(cs).getBlob(with(any(int.class)));
        will(returnValue(blob));
        one(cs).wasNull();
        will(returnValue(false));
        one(blob).length();
        will(returnValue(3l));
        one(blob).getBytes(with(any(long.class)), with(any(int.class)));
        will(returnValue(new byte[]{1, 2, 3}));
      }
    });
    assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) TYPE_HANDLER.getResult(cs, 1));
    mockery.assertIsSatisfied();
  }


}
