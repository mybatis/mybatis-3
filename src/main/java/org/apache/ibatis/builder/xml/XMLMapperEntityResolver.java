package org.apache.ibatis.builder.xml;

import org.apache.ibatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Offline entity resolver for the iBATIS DTDs
 */
public class XMLMapperEntityResolver implements EntityResolver {

  private static final String MAPPER_CONFIG_DTD_RESOURCE = "org/apache/ibatis/builder/xml/ibatis-3-config.dtd";
  private static final String MAPPER_DTD_RESOURCE = "org/apache/ibatis/builder/xml/ibatis-3-mapper.dtd";

  private static final Map<String, String> doctypeMap = new HashMap<String, String>();

  static {
    doctypeMap.put("http://ibatis.apache.org/dtd/ibatis-3-config.dtd".toUpperCase(Locale.ENGLISH), MAPPER_CONFIG_DTD_RESOURCE);
    doctypeMap.put("-//ibatis.apache.org//DTD Config 3.0//EN".toUpperCase(Locale.ENGLISH), MAPPER_CONFIG_DTD_RESOURCE);

    doctypeMap.put("http://ibatis.apache.org/dtd/ibatis-3-mapper.dtd".toUpperCase(Locale.ENGLISH), MAPPER_DTD_RESOURCE);
    doctypeMap.put("-//ibatis.apache.org//DTD Mapper 3.0//EN".toUpperCase(Locale.ENGLISH), MAPPER_DTD_RESOURCE);
  }

  /**
   * Converts a public DTD into a local one
   *
   * @param publicId Unused but required by EntityResolver interface
   * @param systemId The DTD that is being requested
   * @return The InputSource for the DTD
   * @throws org.xml.sax.SAXException If anything goes wrong
   */
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {

    if (publicId != null) publicId = publicId.toUpperCase(Locale.ENGLISH);
    if (systemId != null) systemId = systemId.toUpperCase(Locale.ENGLISH);

    InputSource source = null;
    try {
      String path = doctypeMap.get(publicId);
      source = getInputSource(path, source);
      if (source == null) {
        path = doctypeMap.get(systemId);
        source = getInputSource(path, source);
      }
    } catch (Exception e) {
      throw new SAXException(e.toString());
    }
    return source;
  }

  private InputSource getInputSource(String path, InputSource source) {
    if (path != null) {
      InputStream in;
      try {
        in = Resources.getResourceAsStream(path);
        source = new InputSource(in);
      } catch (IOException e) {
        // ignore, null is ok
      }
    }
    return source;
  }

}