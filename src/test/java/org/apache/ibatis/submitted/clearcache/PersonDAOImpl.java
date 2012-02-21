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
