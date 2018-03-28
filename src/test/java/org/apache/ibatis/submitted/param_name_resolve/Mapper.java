package org.apache.ibatis.submitted.param_name_resolve;

import java.util.List;

public interface Mapper {
    Long getUserCount(List<Integer> ids);
}
