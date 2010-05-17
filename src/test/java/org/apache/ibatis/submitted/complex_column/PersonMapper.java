package org.apache.ibatis.submitted.complex_column;

public interface PersonMapper {
    
    public Person getWithoutComplex(Long id);
    public Person getWithComplex(Long id);
}
