package org.apache.ibatis.builder;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.Reader;

public class XmlConfigBuilderTest {

  @Test
  public void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    XMLConfigBuilder builder = new XMLConfigBuilder(reader);
    Configuration config = builder.parse();
    assertNotNull(config);
  }

}
