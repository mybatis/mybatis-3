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

