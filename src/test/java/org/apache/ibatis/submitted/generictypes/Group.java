package org.apache.ibatis.submitted.generictypes;

import java.util.List;

public class Group {

  private Integer id;
  private User<String> owner;
  private List<String> members;

  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
  public User<String> getOwner() {
    return owner;
  }
  public void setOwner(User<String> owner) {
    this.owner = owner;
  }
  public List<String> getMembers() {
    return members;
  }
  public void setMembers(List<String> members) {
    this.members = members;
  }

}
