package org.apache.ibatis.submitted.refcursor;

import java.util.Map;

public interface OrdersMapper {
    void getOrder1(Map<String, Object> parameter);
    void getOrder2(Map<String, Object> parameter);
}
