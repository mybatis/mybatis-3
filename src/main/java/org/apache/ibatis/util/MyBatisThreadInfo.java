package org.apache.ibatis.util;

import java.util.HashMap;
import java.util.Map;

public class MyBatisThreadInfo {
  private static final ThreadLocal<Map<String, Object>> threadLocal = ThreadLocal.withInitial(() -> {
    return new HashMap<>();
  });

  public static String getTXID() {
    return (String) threadLocal.get().get("TXID");
  }

  public static void setTXID(String txID) {
    threadLocal.get().put("TXID", txID);
  }
}
