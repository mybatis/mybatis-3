package org.apache.ibatis.submitted.automappingcache;

import java.util.List;

public interface Mapper {
  List<List<?>> getMultipleResultSet();
}
