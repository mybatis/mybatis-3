/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.submitted.call_setters_on_nulls_again;

import java.util.List;

public class ChildBean {

  private String name;

  private ChildBean child;

  private List<ChildBean> beans;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChildBean getChild() {
    return child;
  }

  public void setChild(ChildBean child) {
    this.child = child;
  }

  @Override
  public String toString() {
    return "ChildBean [name=" + name + ", child=" + child + ", beans=" + beans + "]";
  }

  public List<ChildBean> getBeans() {
    return beans;
  }

  public void setBeans(List<ChildBean> beans) {
    this.beans = beans;
  }
}
