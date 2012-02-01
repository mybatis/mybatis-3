package org.apache.ibatis.submitted.generictypes;

public class Group {

  private Integer id;
  private User<String> owner;

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

}
