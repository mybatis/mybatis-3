package org.apache.ibatis.submitted.extends_with_constructor;

public interface StudentConstructorMapper {
	public StudentConstructor selectWithTeacherById(Integer id);
	public StudentConstructor selectNoNameById(Integer id);
}
