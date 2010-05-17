package org.apache.ibatis.mapping;

public interface SqlSource {

  BoundSql getBoundSql(Object parameterObject);

}
