package org.apache.ibatis.parsing;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.junit.Test;

public class XPathParserTest {

  @Test
  public void shouldTestXPathParserMethods() throws Exception {
    String resource = "resources/nodelet_test.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XPathParser parser = new XPathParser(inputStream, false, null, null);
    assertEquals((Long)1970l, parser.evalLong("/employee/birth_date/year"));
    assertEquals((short) 6, (short) parser.evalShort("/employee/birth_date/month"));
    assertEquals((Integer) 15, parser.evalInteger("/employee/birth_date/day"));
    assertEquals((Float) 5.8f, parser.evalFloat("/employee/height"));
    assertEquals((Double) 5.8d, parser.evalDouble("/employee/height"));
    assertEquals("${id_var}", parser.evalString("/employee/@id"));
    assertEquals(Boolean.TRUE, parser.evalBoolean("/employee/active"));
    assertEquals("<id>${id_var}</id>", parser.evalNode("/employee/@id").toString().trim());
    assertEquals(7, parser.evalNodes("/employee/*").size());
    XNode node = parser.evalNode("/employee/height");
    assertEquals("employee/height", node.getPath());
    assertEquals("employee[${id_var}]_height", node.getValueBasedIdentifier());
  }

}
