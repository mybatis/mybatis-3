package org.apache.ibatis.submitted.force_flush_on_select;

import java.util.List;

public interface PersonMapper {
    public Person selectByIdFlush(int id);
    public Person selectByIdNoFlush(int id);
    public List<Person> selectAllFlush();
    public List<Person> selectAllNoFlush();
}
