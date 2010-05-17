package com.testdomain;

import java.util.List;

public class Document {

  private int id;
  private String title;
  private DocType type;
  private List attributes;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public DocType getType() {
    return type;
  }

  public void setType(DocType type) {
    this.type = type;
  }

  public List getAttributes() {
    return attributes;
  }

  public void setAttributes(List attributes) {
    this.attributes = attributes;
  }

}
