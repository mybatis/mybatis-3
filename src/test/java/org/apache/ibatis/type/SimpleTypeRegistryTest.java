package org.apache.ibatis.type;

import domain.misc.RichType;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SimpleTypeRegistryTest {

  @Test
  public void shouldTestIfClassIsSimpleTypeAndReturnTrue() {
    assertTrue(SimpleTypeRegistry.isSimpleType(String.class));
  }

  @Test
  public void shouldTestIfClassIsSimpleTypeAndReturnFalse() {
    assertFalse(SimpleTypeRegistry.isSimpleType(RichType.class));
  }

}
