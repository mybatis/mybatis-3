package com.testdomain;

import java.io.Serializable;
import java.util.List;

public class Topic implements Serializable {

  private String topicTitle;
  private List descriptionList;

  /**
   * @return Returns the descriptionList.
   */
  public List getDescriptionList() {
    return descriptionList;
  }

  /**
   * @param descriptionList The descriptionList to set.
   */
  public void setDescriptionList(List description) {
    this.descriptionList = description;
  }

  /**
   * @return Returns the topicTitle.
   */
  public String getTopicTitle() {
    return topicTitle;
  }

  /**
   * @param topicTitle The topicTitle to set.
   */
  public void setTopicTitle(String topicTitle) {
    this.topicTitle = topicTitle;
  }
}