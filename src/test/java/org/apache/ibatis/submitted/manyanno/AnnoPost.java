package org.apache.ibatis.submitted.manyanno;

import java.util.List;

public class AnnoPost {

  private int id;
  private String subject;
  private String body;

  private List<AnnoPostTag> tags;

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public List<AnnoPostTag> getTags() {
    return tags;
  }

  public void setTags(List<AnnoPostTag> tags) {
    this.tags = tags;
  }
}
