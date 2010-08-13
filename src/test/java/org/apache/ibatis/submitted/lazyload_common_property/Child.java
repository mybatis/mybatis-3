package org.apache.ibatis.submitted.lazyload_common_property;

public class Child {
    private Integer id;
    private String name;
    private Father father;
    private GrandFather grandFather;
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
	public GrandFather getGrandFather() {
		return grandFather;
	}
	public void setGrandFather(GrandFather grandFather) {
		this.grandFather = grandFather;
	}
}
