package com.ibatis.jpetstore.persistence;

import com.ibatis.dao.client.DaoManager;
import com.ibatis.jpetstore.persistence.iface.CategoryDao;
import junit.framework.TestCase;

public class DaoManagerTest extends TestCase {

  public void testShouldGetDaoManagerInstance() {
    DaoManager daoMgr = DaoConfig.getDaoManager();
    assertNotNull(daoMgr);
    CategoryDao catDao = (CategoryDao) daoMgr.getDao(CategoryDao.class);
    assertNotNull(catDao);
  }

}
