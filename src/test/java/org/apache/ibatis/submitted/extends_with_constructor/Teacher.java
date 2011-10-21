package org.apache.ibatis.submitted.extends_with_constructor;

import java.util.List;

public class Teacher {
	private int id;
	private String name;
	private List<StudentConstructor> students;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<StudentConstructor> getStudents() {
		return students;
	}
	public void setStudents(List<StudentConstructor> students) {
		this.students = students;
	}
}
