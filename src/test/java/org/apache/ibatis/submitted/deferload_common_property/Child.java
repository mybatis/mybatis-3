package org.apache.ibatis.submitted.deferload_common_property;

public class Child {
    private Integer id;
    private String name;
    private Father father;
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
	public Father getFather() {
		return father;
	}
	public void setFather(Father father) {
		this.father = father;
	}
}
