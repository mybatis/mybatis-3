package com.ibatis.jpetstore.persistence;

import com.ibatis.jpetstore.persistence.iface.SequenceDao;

public class SequenceDaoTest extends BasePersistenceTest {

  private SequenceDao seqDao = (SequenceDao) daoMgr.getDao(SequenceDao.class);

  public void testSequenceShouldReturnIncrementalIdNumbers() {
    String sequenceKey = "ordernum";
    int nextId = seqDao.getNextId(sequenceKey);
    int expected = nextId + 1;
    int actual = seqDao.getNextId(sequenceKey);
    assertEquals(expected, actual);
  }

}
