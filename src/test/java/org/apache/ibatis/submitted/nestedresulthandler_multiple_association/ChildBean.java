package org.apache.ibatis.submitted.nestedresulthandler_multiple_association;

public class ChildBean {
	private Integer id;
	private String value;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ChildBean [id=" + id + ", value=" + value + "]";
	}
}
