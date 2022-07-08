package org.apache.ibatis.reflection.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyNamerTest {

  @Test
  void methodToProperty() {
    assertEquals("ok", PropertyNamer.methodToProperty("isOk"));
    assertEquals("OK", PropertyNamer.methodToProperty("isOK"));

    assertEquals("name", PropertyNamer.methodToProperty("getName"));
    assertEquals("XName", PropertyNamer.methodToProperty("getXName"));
    assertEquals("xName", PropertyNamer.methodToProperty("getxName"));

    assertEquals("name", PropertyNamer.methodToProperty("setName"));
    assertEquals("XName", PropertyNamer.methodToProperty("setXName"));
    assertEquals("xName", PropertyNamer.methodToProperty("setxName"));
  }
}
