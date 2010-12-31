package org.apache.ibatis.builder;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class XmlConfigBuilderTest {

  @Test
  public void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
    String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
    Configuration config = builder.parse();
    assertNotNull(config);
  }

}
