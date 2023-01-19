/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
  protected final String name;

  protected PropertyTokenizer(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return null;
  }

  public String getChildren() {
    return null;
  }

  public String getIndexedName() {
    return name;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public PropertyTokenizer next() {
    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }

  public static PropertyTokenizer valueOf(String fullname) {
    int delimDot = -1;
    int delimBracket = -1;
    int length = fullname.length();
    for (int i = 0; i < length; i++) {
      char c = fullname.charAt(i);
      if (c == '[') {
        delimBracket = i;
      } else if (c == '.') {
        delimDot = i;
        break;
      }
    }

    if (delimDot < 0 && delimBracket < 0) {
      return new PropertyTokenizer(fullname);
    }

    String name = fullname, children = null;
    if (delimDot >= 0) {
      name = fullname.substring(0, delimDot);
      children = fullname.substring(delimDot + 1);
    }

    if (delimBracket <= 0) {
      return new IterablePropertyTokenizer(name, children);
    } else {
      String index, indexedName = name;
      index = name.substring(delimBracket + 1, name.length() - 1);
      name = name.substring(0, delimBracket);
      return new IndexedPropertyTokenizer(name, children, index, indexedName);
    }
  }

  private static class IterablePropertyTokenizer extends PropertyTokenizer {

    private final String children;

    IterablePropertyTokenizer(String name, String children) {
      super(name); this.children = children;
    }

    @Override
    public String getChildren() {
      return this.children;
    }

    @Override
    public boolean hasNext() {
      return this.children != null;
    }

    @Override
    public PropertyTokenizer next() {
      return valueOf(this.children);
    }
  }

  private static class IndexedPropertyTokenizer extends IterablePropertyTokenizer {

    private final String index;
    private final String indexedName;

    IndexedPropertyTokenizer(String name, String children, String index, String indexedName) {
      super(name, children); this.index = index; this.indexedName = indexedName;
    }

    @Override
    public String getIndex() {
      return index;
    }

    @Override
    public String getIndexedName() {
      return indexedName;
    }
  }
}

