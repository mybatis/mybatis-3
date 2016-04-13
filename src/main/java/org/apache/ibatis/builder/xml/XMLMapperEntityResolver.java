/**
 *    Copyright 2009-2015 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.ibatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Offline entity resolver for the MyBatis XSDs
 * 
 * @author Eduardo Macarron
 */
public class XMLMapperEntityResolver implements EntityResolver {

  private static final String MYBATIS_MAPPER = "mybatis-3-mapper".toUpperCase(Locale.ENGLISH);
  private static final String MYBATIS_CONFIG = "mybatis-3-config".toUpperCase(Locale.ENGLISH);

  private static final String MYBATIS_CONFIG_XSD = "org/apache/ibatis/builder/xml/mybatis-3-config.xsd";
  private static final String MYBATIS_MAPPER_XSD = "org/apache/ibatis/builder/xml/mybatis-3-mapper.xsd";

  /*
   * Converts a public DTD into a local one
   * 
   * @param publicId The public id that is what comes after "PUBLIC"
   * @param systemId The system id that is what comes after the public id.
   * @return The InputSource for the DTD
   * 
   * @throws org.xml.sax.SAXException If anything goes wrong
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException {

    if (systemId != null) {     
      systemId = systemId.toUpperCase(Locale.ENGLISH);
      if (systemId.contains(MYBATIS_CONFIG)) {
        return getInputSource(MYBATIS_CONFIG_XSD);       
      } else if (systemId.contains(MYBATIS_MAPPER)){
        return getInputSource(MYBATIS_MAPPER_XSD);
      }
    }
    return null;
  }

  private InputSource getInputSource(String path) {
    InputSource source = null;
    if (path != null) {
      try {
        InputStream in = Resources.getResourceAsStream(path);
        source = new InputSource(in);
      } catch (IOException e) {
        // ignore, null is ok
      }
    }
    return source;
  }

}