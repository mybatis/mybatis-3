package org.apache.ibatis.submitted.permissions;

import java.util.ArrayList;
import java.util.List;

public class Resource {
  private String name;
  private List<Principal> Principals = new ArrayList<Principal>();

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public List<Principal> getPrincipals() {
    return Principals;
  }
  public void setPrincipals(List<Principal> principals) {
    this.Principals = principals;
  }
}