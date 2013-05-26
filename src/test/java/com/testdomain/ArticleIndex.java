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
import java.util.List;

public class ArticleIndex implements Serializable {

  private String categoryTitle;
  private List topics;

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
   * @return Returns the topics.
   */
  public List getTopics() {
    return topics;
  }

  /*
   * @param topics The topics to set.
   */
  public void setTopics(List topics) {
    this.topics = topics;
  }
}