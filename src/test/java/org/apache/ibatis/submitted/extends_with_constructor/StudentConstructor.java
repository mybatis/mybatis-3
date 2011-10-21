package org.apache.ibatis.submitted.extends_with_constructor;

import java.util.LinkedList;
import java.util.List;

public class StudentConstructor {
	public enum Constructor {
		ID,
		ID_NAME
	}
	private List<Constructor> constructors = new LinkedList<Constructor>();
	private final int id;
	private String name;
	private Teacher teacher;
	public StudentConstructor(Integer id) {
		constructors.add(Constructor.ID);
		this.id = id;
	}
	public StudentConstructor(Integer id, String name) {
		constructors.add(Constructor.ID_NAME);
		this.id = id;
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Teacher getTeacher() {
		return teacher;
	}
	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
	public List<Constructor> getConstructors() {
		return constructors;
	}
}
