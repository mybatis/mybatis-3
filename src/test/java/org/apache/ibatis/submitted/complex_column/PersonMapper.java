package org.apache.ibatis.submitted.complex_column;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface PersonMapper {
    
    public Person getWithoutComplex(Long id);
    public Person getWithComplex(Long id);
    
    @Select({
      "SELECT id, firstName, lastName, parent_id, parent_firstName, parent_lastName",
      "FROM Person",
      "WHERE id = #{id,jdbcType=INTEGER}"
    })
    @ResultMap("personMapComplex")
    public Person getWithComplex2(Long id);

    @Select({
        "SELECT id, firstName, lastName, parent_id, parent_firstName, parent_lastName",
        "FROM Person",
        "WHERE id = #{id,jdbcType=INTEGER}"
      })
    @ResultMap("org.apache.ibatis.submitted.complex_column.PersonMapper.personMapComplex")
    public Person getWithComplex3(Long id);
}
