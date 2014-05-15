package org.apache.ibatis.submitted.one_parameterprovider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateParameterProvider {
  public Object getParametersWithDate(Object value, Class<?> type, List<Object> originalParameters) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("0", originalParameters.get(0));
    parameters.put("1", value);
    return parameters;
  }
}
