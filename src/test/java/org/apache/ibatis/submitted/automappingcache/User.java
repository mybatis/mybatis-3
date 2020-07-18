package org.apache.ibatis.submitted.automappingcache;

public class User {
  private Integer id;
  private String name;

  private InnerEntity innerEntity;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InnerEntity getInnerEntity() {
    return innerEntity;
  }

  public void setInnerEntity(InnerEntity innerEntity) {
    this.innerEntity = innerEntity;
  }
}
