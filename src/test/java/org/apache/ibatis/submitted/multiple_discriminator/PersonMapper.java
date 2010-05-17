package org.apache.ibatis.submitted.multiple_discriminator;

public interface PersonMapper {
    
    public Person get(Long id);
    public Person get2(Long id);
    public Person getLoop();
}
