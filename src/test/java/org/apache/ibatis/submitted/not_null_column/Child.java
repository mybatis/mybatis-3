package org.apache.ibatis.submitted.not_null_column;

public class Child {
    private Integer id;
    private Integer fatherId;
    private String name;
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

    public Integer getFatherId()
    {
        return fatherId;
    }

    public void setFatherId(Integer fatherId)
    {
        this.fatherId = fatherId;
    }
}
