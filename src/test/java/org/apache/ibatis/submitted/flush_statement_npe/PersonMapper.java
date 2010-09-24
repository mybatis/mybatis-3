package org.apache.ibatis.submitted.flush_statement_npe;

public interface PersonMapper {
    public Person selectById(int id);
    public void update(Person person);
}
