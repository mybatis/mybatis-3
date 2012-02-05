package org.apache.ibatis.submitted.permissions;

import java.util.ArrayList;
import java.util.List;

public class Principal {
  private String resourceName;
  private String principalName;
  private List<String> permissions = new ArrayList<String>();
  
  public String getPrincipalName() {
    return principalName;
  }
  public void setPrincipalName(String principalName) {
    this.principalName = principalName;
  }
  public List<String> getPermissions() {
    return permissions;
  }
  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }
}