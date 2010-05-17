package org.apache.ibatis.session;

public interface ResultHandler {

  void handleResult(ResultContext context);

}
