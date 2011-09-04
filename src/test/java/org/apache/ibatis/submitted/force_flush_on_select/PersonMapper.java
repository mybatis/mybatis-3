package org.apache.ibatis.submitted.force_flush_on_select;

import java.util.List;

public interface PersonMapper {
    public Person selectById(int id);
    public List<Person> selectAll();
    public void update(Person person);
}
