/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.dao.engine.builder.xml;

import com.ibatis.common.resources.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DaoClasspathEntityResolver implements EntityResolver {

  private static final String DTD_PATH_DAO = "com/ibatis/dao/engine/builder/xml/dao-2.dtd";

  private static final Map doctypeMap = new HashMap();

  static {
    doctypeMap.put("http://www.ibatis.com/dtd/dao-2.dtd", DTD_PATH_DAO);
    doctypeMap.put("http://ibatis.apache.org/dtd/dao-2.dtd", DTD_PATH_DAO);
    doctypeMap.put("-//iBATIS.com//DTD DAO Configuration 2.0", DTD_PATH_DAO);
    doctypeMap.put("-//iBATIS.com//DTD DAO Config 2.0", DTD_PATH_DAO);
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
    InputSource source = null;

    try {
      String path = (String) doctypeMap.get(publicId);
      source = getInputSource(path, source);
      if (source == null) {
        path = (String) doctypeMap.get(systemId);
        source = getInputSource(path, source);
      }
    } catch (Exception e) {
      throw new SAXException(e.toString());
    }

    return source;
  }

  private InputSource getInputSource(String path, InputSource source) {
    if (path != null) {
      InputStream in = null;
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
