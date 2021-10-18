package org.apache.ibatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnumAliasTypeHandlerTest extends BaseTypeHandlerTest {

  enum MyEnum implements EnumAlias {
    JavaScript("JS"),
    TypeScript("TS"),
    Kubernetes("K8S");

    private final String fullName;

    MyEnum(String fullName) {
      this.fullName = fullName;
    }

    @Override
    public String getAlias() {
      return fullName;
    }
  }

  private static final TypeHandler<MyEnum> TYPE_HANDLER =
    new EnumAliasTypeHandler<>(MyEnum.class);


  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, MyEnum.JavaScript, null);
    verify(ps).setString(1, MyEnum.JavaScript.getAlias());
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.VARCHAR);
    verify(ps).setNull(1, JdbcType.VARCHAR.TYPE_CODE);
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getString("column")).thenReturn("JS");
    assertEquals(MyEnum.JavaScript, TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getString(1)).thenReturn("JS");
    assertEquals(MyEnum.JavaScript, TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getString(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getString(1)).thenReturn("JS");
    assertEquals(MyEnum.JavaScript, TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getString(1)).thenReturn(null);
    when(cs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }

}
