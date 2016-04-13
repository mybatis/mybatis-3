package org.apache.ibatis.builder.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class XMLNamespaceContext implements NamespaceContext {

  Map<String, String> PREF_MAP = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L;
    {
      put("config", "http://mybatis.org/schema/mybatis-3-config");
      put("mapper", "http://mybatis.org/schema/mybatis-3-mapper");
    }
  };

  @Override
  public String getNamespaceURI(String prefix) {
    return PREF_MAP.get(prefix);
  }

  @Override
  public String getPrefix(String uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    throw new UnsupportedOperationException();
  }

}