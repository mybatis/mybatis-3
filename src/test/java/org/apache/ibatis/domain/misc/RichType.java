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
package org.apache.ibatis.domain.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichType {

  private RichType richType;

  private String richField;

  private String richProperty;

  private Map richMap = new HashMap();

  private List richList = new ArrayList() {
    {
      add("bar");
    }
  };

  public RichType getRichType() {
    return richType;
  }

  public void setRichType(RichType richType) {
    this.richType = richType;
  }

  public String getRichProperty() {
    return richProperty;
  }

  public void setRichProperty(String richProperty) {
    this.richProperty = richProperty;
  }

  public List getRichList() {
    return richList;
  }

  public void setRichList(List richList) {
    this.richList = richList;
  }

  public Map getRichMap() {
    return richMap;
  }

  public void setRichMap(Map richMap) {
    this.richMap = richMap;
  }
}
