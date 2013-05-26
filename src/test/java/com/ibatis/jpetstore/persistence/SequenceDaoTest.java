/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
