package org.apache.ibatis.type;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TypeAliasRegistryTest {

  @Test
  public void shouldRegisterAndResolveTypeAlias() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    typeAliasRegistry.registerAlias("rich", "domain.misc.RichType");

    assertEquals("domain.misc.RichType", typeAliasRegistry.resolveAlias("rich").getName());
  }

  @Test
  public void shouldFetchArrayType() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    assertEquals(Byte[].class, typeAliasRegistry.resolveAlias("byte[]"));
  }

}
