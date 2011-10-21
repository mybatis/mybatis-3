package org.apache.ibatis.submitted.extends_with_constructor;

public class Student {
	private int id;
	private int name;
	private Teacher teacher;
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public int getName() {
		return name;
	}
	public void setName(int name) {
		this.name = name;
	}
	public Teacher getTeacher() {
		return teacher;
	}
	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
}
