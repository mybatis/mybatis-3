package org.apache.ibatis.submitted.xml_external_ref;

import java.io.Serializable;
import java.util.List;

public class Person implements Serializable {
  private static final long serialVersionUID = 1L;

  private Integer id;

  private String name;

  private List<Pet> pets;

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

  public List<Pet> getPets() {
    return pets;
  }

  public void setPets(List<Pet> pets) {
    this.pets = pets;
  }
}
