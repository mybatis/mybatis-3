package com.ibatis.jpetstore.persistence;

import com.ibatis.dao.client.DaoManager;
import junit.framework.TestCase;

public class BasePersistenceTest extends TestCase {
  protected DaoManager daoMgr = PersistenceFixture.getDaoManager();

  public void testDummy() {
    //to avoid warnings
  }

}
