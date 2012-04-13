/*
 *    Copyright 2009-2012 The MyBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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

/*
 * Offline entity resolver for the iBATIS DTDs
 */
public class XMLMapperEntityResolver implements EntityResolver {

  private static final Map<String, String> doctypeMap = new HashMap<String, String>();

  private static final String IBATIS_CONFIG_DOCTYPE = "-//ibatis.apache.org//DTD Config 3.0//EN".toUpperCase(Locale.ENGLISH);
  private static final String IBATIS_CONFIG_URL = "http://ibatis.apache.org/dtd/ibatis-3-config.dtd".toUpperCase(Locale.ENGLISH);

  private static final String IBATIS_MAPPER_DOCTYPE = "-//ibatis.apache.org//DTD Mapper 3.0//EN".toUpperCase(Locale.ENGLISH);
  private static final String IBATIS_MAPPER_URL = "http://ibatis.apache.org/dtd/ibatis-3-mapper.dtd".toUpperCase(Locale.ENGLISH);

  private static final String MYBATIS_CONFIG_DOCTYPE = "-//mybatis.org//DTD Config 3.0//EN".toUpperCase(Locale.ENGLISH);
  private static final String MYBATIS_CONFIG_URL = "http://mybatis.org/dtd/mybatis-3-config.dtd".toUpperCase(Locale.ENGLISH);

  private static final String MYBATIS_MAPPER_DOCTYPE = "-//mybatis.org//DTD Mapper 3.0//EN".toUpperCase(Locale.ENGLISH);
  private static final String MYBATIS_MAPPER_URL = "http://mybatis.org/dtd/mybatis-3-mapper.dtd".toUpperCase(Locale.ENGLISH);

  private static final String IBATIS_CONFIG_DTD = "org/apache/ibatis/builder/xml/mybatis-3-config.dtd";
  private static final String IBATIS_MAPPER_DTD = "org/apache/ibatis/builder/xml/mybatis-3-mapper.dtd";

  static {
    doctypeMap.put(IBATIS_CONFIG_URL, IBATIS_CONFIG_DTD);
    doctypeMap.put(IBATIS_CONFIG_DOCTYPE, IBATIS_CONFIG_DTD);

    doctypeMap.put(IBATIS_MAPPER_URL, IBATIS_MAPPER_DTD);
    doctypeMap.put(IBATIS_MAPPER_DOCTYPE, IBATIS_MAPPER_DTD);

    doctypeMap.put(MYBATIS_CONFIG_URL, IBATIS_CONFIG_DTD);
    doctypeMap.put(MYBATIS_CONFIG_DOCTYPE, IBATIS_CONFIG_DTD);

    doctypeMap.put(MYBATIS_MAPPER_URL, IBATIS_MAPPER_DTD);
    doctypeMap.put(MYBATIS_MAPPER_DOCTYPE, IBATIS_MAPPER_DTD);
  }

  /*
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