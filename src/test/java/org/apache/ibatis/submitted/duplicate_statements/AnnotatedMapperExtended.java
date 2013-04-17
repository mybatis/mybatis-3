package org.apache.ibatis.submitted.duplicate_statements;

import java.util.List;

import org.apache.ibatis.annotations.Select;

/**
 * This interface should fail when added to the configuration.  It has
 * a method with the same name, but different parameters, as a method
 * in the super interface  
 *
 */
public interface AnnotatedMapperExtended extends AnnotatedMapper {

    @Select("select * from users")
    List<User> getAllUsers(int i);
}
