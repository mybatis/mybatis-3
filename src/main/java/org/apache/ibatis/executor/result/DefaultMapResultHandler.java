package org.apache.ibatis.executor.result;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.HashMap;
import java.util.Map;

public class DefaultMapResultHandler implements ResultHandler {

  private final Map mappedResults = new HashMap();
  private final String mapKey;

  public DefaultMapResultHandler(String mapKey) {
    this.mapKey = mapKey;
  }

  public void handleResult(ResultContext context) {
    final Object value = context.getResultObject();
    final MetaObject mo = MetaObject.forObject(value);
    final Object key = mo.getValue(mapKey);
    mappedResults.put(key, value);
  }

  public Map getMappedResults() {
    return mappedResults;
  }
}
