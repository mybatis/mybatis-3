package org.apache.ibatis.submitted.enumtypehandler_on_map;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface PersonMapper {
    
    public static interface TypeName {
        public Person.Type getType();
        public String getName();
    }
    
    public List<Person> getByType(@Param("type") Person.Type type, @Param("name") String name);
    public List<Person> getByTypeNoParam(TypeName typeName);
    
}
