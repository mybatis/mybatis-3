package org.apache.ibatis.submitted.permissions;

import java.util.ArrayList;
import java.util.List;

public class Resource {
  private String name;
  private List<PrincipalPermissions> permissions = new ArrayList<PrincipalPermissions>();

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public List<PrincipalPermissions> getPermissions() {
    return permissions;
  }
  public void setPermissions(List<PrincipalPermissions> permissions) {
    this.permissions = permissions;
  }
}