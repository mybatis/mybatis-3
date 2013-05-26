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
package com.testdomain;

import java.io.Serializable;

/*
 * Denormalized version of an toy ArticleIndex object.
 */
public class ArticleIndexDenorm implements Serializable {

  private String categoryTitle;

  private String topicTitle;

  private String description;

  /*
   * @return Returns the categoryTitle.
   */
  public String getCategoryTitle() {
    return categoryTitle;
  }

  /*
   * @param categoryTitle The categoryTitle to set.
   */
  public void setCategoryTitle(String categoryTitle) {
    this.categoryTitle = categoryTitle;
  }

  /*
   * @return Returns the description.
   */
  public String getDescription() {
    return description;
  }

  /*
   * @param description The description to set.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /*
   * @return Returns the topicTitle.
   */
  public String getTopicTitle() {
    return topicTitle;
  }

  /*
   * @param topicTitle The topicTitle to set.
   */
  public void setTopicTitle(String topicTitle) {
    this.topicTitle = topicTitle;
  }
}