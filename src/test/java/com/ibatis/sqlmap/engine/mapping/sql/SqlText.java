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
package com.ibatis.sqlmap.engine.mapping.sql;

import org.apache.ibatis.mapping.ParameterMapping;

import java.util.List;

public class SqlText implements SqlChild {

  private String text;
  private boolean isWhiteSpace;
  private boolean postParseRequired;

  private List<ParameterMapping> parameterMappings;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text.replace('\r', ' ').replace('\n', ' ');
    this.isWhiteSpace = text.trim().length() == 0;
  }

  public boolean isWhiteSpace() {
    return isWhiteSpace;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  public void setParameterMappings(List<ParameterMapping> parameterMappings) {
    this.parameterMappings = parameterMappings;
  }

  public boolean isPostParseRequired() {
    return postParseRequired;
  }

  public void setPostParseRequired(boolean postParseRequired) {
    this.postParseRequired = postParseRequired;
  }

}

