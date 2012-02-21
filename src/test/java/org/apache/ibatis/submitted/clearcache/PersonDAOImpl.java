/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.submitted.clearcache;

import org.apache.ibatis.session.SqlSession;

public class PersonDAOImpl implements PersonDAO {
  
  SqlSession session;
  
  public PersonDAOImpl(SqlSession session) {
    this.session = session;
  }
  
  private SqlSession getSqlSession() {
    return session;
  }
  
	public void create(Person person) {
		getSqlSession().insert("create", person);
		
		for(Address address : person.getAddresses()) {
			address.setPersonId(person.getId());
			getSqlSession().insert("createAddress", address);
		}		
	}
	
	public Person findById(int id) {
		return getSqlSession().selectOne("findById", id);
	}

	public void update(Person person) {
		getSqlSession().update("update", person);		
	}

	public void delete(int id) {
		getSqlSession().delete("delete", id);	
	}

	public void deleteAddress(int addressId) {
//		System.out.println(getSqlSession().getConfiguration().getCacheNames());
		//Cache cache = getSqlSession().getConfiguration().getCache("Person");
		//cache.clear();
		getSqlSession().delete("deleteAddress", addressId);
	}
	
	
}
