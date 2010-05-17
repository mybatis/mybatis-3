package org.apache.ibatis.session;

public interface ResultContext {

  Object getResultObject();

  int getResultCount();

  boolean isStopped();

  void stop();

}
