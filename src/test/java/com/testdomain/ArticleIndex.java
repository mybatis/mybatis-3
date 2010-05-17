package com.testdomain;

import java.io.Serializable;
import java.util.List;

public class ArticleIndex implements Serializable {

  private String categoryTitle;
  private List topics;

  /**
   * @return Returns the categoryTitle.
   */
  public String getCategoryTitle() {
    return categoryTitle;
  }

  /**
   * @param categoryTitle The categoryTitle to set.
   */
  public void setCategoryTitle(String categoryTitle) {
    this.categoryTitle = categoryTitle;
  }

  /**
   * @return Returns the topics.
   */
  public List getTopics() {
    return topics;
  }

  /**
   * @param topics The topics to set.
   */
  public void setTopics(List topics) {
    this.topics = topics;
  }
}