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
    StringBuilder sb = new StringBuilder("ParentBean [id=" + id + ", value="
        + value + "]\nChilds:\n");
    for (Binome<ChildBean, ChildBean> binome : childs) {
      sb.append("\tChild : ").append(binome).append('\n');
    }
    return sb.toString();
  }
}
