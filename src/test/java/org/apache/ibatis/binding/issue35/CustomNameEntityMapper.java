package org.apache.ibatis.binding.issue35;

import java.util.List;

/**
 * We extend here the "generated" mapper with customized logic
 */
public interface CustomNameEntityMapper extends GeneratedNameEntityMapper {

    List<NameEntity> selectOlderThan(int age);
}
