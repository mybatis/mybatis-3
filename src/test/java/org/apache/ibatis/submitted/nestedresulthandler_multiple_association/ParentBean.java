/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.nestedresulthandler_multiple_association;

import java.util.List;

public class ParentBean {
  private Integer id;
  private String value;
  private List<Binome<ChildBean, ChildBean>> childs;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Binome<ChildBean, ChildBean>> getChilds() {
    return childs;
  }

  public void setChilds(List<Binome<ChildBean, ChildBean>> childs) {
    this.childs = childs;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ParentBean [id=" + id + ", value=" + value + "]\nChilds:\n");
    for (Binome<ChildBean, ChildBean> binome : childs) {
      sb.append("\tChild : ").append(binome).append('\n');
    }
    return sb.toString();
  }
}
