package com.testdomain;

import java.io.Serializable;

public class TopicDescription implements Serializable {

  private String description;

  /**
   * @return Returns the topicDescription.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param topicDescription The topicDescription to set.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}