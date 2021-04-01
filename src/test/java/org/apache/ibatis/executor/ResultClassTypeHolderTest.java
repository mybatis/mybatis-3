package org.apache.ibatis.executor;

import org.apache.ibatis.executor.result.ResultClassTypeHolder;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class ResultClassTypeHolderTest {

  @Test
  public void getResultClassFromTl() {
    try {
      ResultClassTypeHolder.setResultType(Object.class);
      getResultClass();
    }finally {
      ResultClassTypeHolder.clean();
    }

  }

  private void getResultClass() {
    Class aClass = ResultClassTypeHolder.getResultType();
    Assert.assertNotNull(aClass);
  }

}
