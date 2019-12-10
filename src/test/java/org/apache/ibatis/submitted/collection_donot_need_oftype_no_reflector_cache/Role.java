package org.apache.ibatis.submitted.collection_donot_need_oftype_no_reflector_cache;

public class Role {
    private Integer id;	//int
    private String roleName;//varchar

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }
}
