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
package com.ibatis.jpetstore.persistence.sqlmapdao;

import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.jpetstore.domain.Sequence;
import com.ibatis.jpetstore.persistence.iface.SequenceDao;

public class SequenceSqlMapDao extends BaseSqlMapDao implements SequenceDao {

  public SequenceSqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  /*
   * This is a generic sequence ID generator that is based on a database
   * table called 'SEQUENCE', which contains two columns (NAME, NEXTID).
   * <p/>
   * This approach should work with any database.
   *
   * @param name The name of the sequence.
   * @return The Next ID
   * @
   */
  public synchronized int getNextId(String name) {
    Sequence sequence = new Sequence(name, -1);

    sequence = (Sequence) queryForObject("getSequence", sequence);
    if (sequence == null) {
      throw new DaoException("Error: A null sequence was returned from the database (could not get next " + name + " sequence).");
    }
    Object parameterObject = new Sequence(name, sequence.getNextId() + 1);
    update("updateSequence", parameterObject);

    return sequence.getNextId();
  }

}
