package com.testdomain;

import java.io.Serializable;

/**
 * Denormalized version of an toy ArticleIndex object.
 */
public class ArticleIndexDenorm implements Serializable {

  private String categoryTitle;

  private String topicTitle;

  private String description;

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
   * @return Returns the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description The description to set.
   */
  public void setDescription(String description) {
    this.description = description;
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