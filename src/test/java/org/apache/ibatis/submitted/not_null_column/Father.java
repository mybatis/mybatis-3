package org.apache.ibatis.submitted.not_null_column;

import java.util.List;

public class Father {
    private Integer id;
    private String name;
    private List<Child> children;
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
	public List<Child> getChildren() {
		return children;
	}
	public void setChildren(List<Child> children) {
		this.children = children;
	}
}
