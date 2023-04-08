package org.apache.ibatis.submitted.mapkey_value;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.MapValue;

import java.util.Map;

public interface NoticeMapper {

  @MapKey("status")
  @MapValue("count")
  Map<Integer, Integer> groupStatus();
}
