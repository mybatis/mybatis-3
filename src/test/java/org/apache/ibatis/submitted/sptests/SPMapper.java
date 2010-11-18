package org.apache.ibatis.submitted.sptests;

import java.util.List;
import java.util.Map;

public interface SPMapper {
    Object adder(Parameter parameter);
    void adder2(Parameter parameter);
    Name getName(Integer id);
    List<Name> getNames(Map<String, Object> parms);
    List<Name> getNamesWithArray(Map<String, Object> parms);
}
