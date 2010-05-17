package org.apache.ibatis.submitted.ognl_enum;

import java.util.List;

public interface PersonMapper {
    
    public interface PersonType {
        public Person.Type getType();
    }
    
    public List<Person> selectAllByType(Person.Type type);
    public List<Person> selectAllByTypeNameAttribute(Person.Type type);
    public List<Person> selectAllByTypeWithInterface(PersonType personType);
    public List<Person> selectAllByTypeNameAttributeWithInterface(PersonType personType);
}
