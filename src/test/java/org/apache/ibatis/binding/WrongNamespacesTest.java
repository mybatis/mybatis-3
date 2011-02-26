package org.apache.ibatis.binding;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class WrongNamespacesTest {

  @Test(expected=BuilderException.class)
  public void shouldFailForWrongNamespace() throws Exception {
    Configuration configuration = new Configuration();
    configuration.addMapper(WrongNamespaceMapper.class);
  }

  @Test(expected=BuilderException.class)
  public void shouldFailForMissingNamespace() throws Exception {
    Configuration configuration = new Configuration();
    configuration.addMapper(MissingNamespaceMapper.class);
  }


}
