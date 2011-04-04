package org.apache.ibatis.submitted.xml_external_ref;

import java.util.Map;

public interface InvalidWithInsertMapper {
  Map<?, ?> selectAll();
  void insert(Person person);
}
