package org.apache.ibatis.submitted.xml_external_ref;

import java.io.Serializable;

public class Pet implements Serializable {
  private static final long serialVersionUID = 1L;

  private Integer id;

  private Integer ownerId;

  private String name;

  private Person owner;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Integer ownerId) {
    this.ownerId = ownerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Person getOwner() {
    return owner;
  }

  public void setOwner(Person owner) {
    this.owner = owner;
  }
}
